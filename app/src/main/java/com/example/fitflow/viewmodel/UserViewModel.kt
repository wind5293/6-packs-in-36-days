package com.example.fitflow.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitflow.data.ExerciseRepository
import com.example.fitflow.data.StepCounterManager
import com.example.fitflow.data.UserPreferences
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.DailyHealthMetrics
import com.example.fitflow.data.model.FitnessGoal
import com.example.fitflow.data.model.StepSource
import com.example.fitflow.data.model.UserProfile
import com.example.fitflow.domain.WorkoutPlanGenerator
import com.example.fitflow.notification.WorkoutReminderReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.example.fitflow.data.model.Exercise
import com.example.fitflow.data.model.WorkoutExercise
import com.example.fitflow.utils.GifDownloadManager
import com.example.fitflow.utils.GifUrlHelper
import com.example.fitflow.utils.NetworkStateHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import java.io.File

data class PlanProvisioningState(
    val progress: Float = 0f,
    val isCompleted: Boolean = false,
    val requiresMobileDataConsent: Boolean = false,
    val isNoNetwork: Boolean = false,
    val isInProgress: Boolean = false,
    val hasError: Boolean = false,
    val statusMessage: String = "Creating your plan..."
)

data class MediaPackStatus(
    val isReady: Boolean = false,
    val downloaded: Int = 0,
    val total: Int = 0,
    val failedCount: Int = 0,
    val isSyncing: Boolean = false
)

class UserViewModel(
    private val userPreferences: UserPreferences,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    private val _userProfile: MutableStateFlow<UserProfile?> = MutableStateFlow(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    val allExercises: StateFlow<List<Exercise>> = exerciseRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _workoutPlan = MutableStateFlow<List<DayPlan>>(emptyList())
    val workoutPlan: StateFlow<List<DayPlan>> = _workoutPlan.asStateFlow()

    private val _completedDays = MutableStateFlow<Set<Int>>(emptySet())
    val completedDays: StateFlow<Set<Int>> = _completedDays.asStateFlow()

    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate.asStateFlow()

    private val _weightHistory = MutableStateFlow<List<Pair<LocalDate, Float>>>(emptyList())
    val weightHistory: StateFlow<List<Pair<LocalDate, Float>>> = _weightHistory.asStateFlow()

    private val _todayHealthMetrics = MutableStateFlow(
        DailyHealthMetrics(
            date = LocalDate.now(),
            steps = 0,
            waterIntakeMl = 0,
            waterGoalMl = 2000,
            stepSource = StepSource.MANUAL
        )
    )
    val todayHealthMetrics: StateFlow<DailyHealthMetrics> = _todayHealthMetrics.asStateFlow()

    private val _healthMetricsHistory = MutableStateFlow<List<DailyHealthMetrics>>(emptyList())
    val healthMetricsHistory: StateFlow<List<DailyHealthMetrics>> = _healthMetricsHistory.asStateFlow()

    private val _activityRecognitionGranted = MutableStateFlow(false)
    val activityRecognitionGranted: StateFlow<Boolean> = _activityRecognitionGranted.asStateFlow()

    private val _stepSensorEnabled = MutableStateFlow(false)
    val stepSensorEnabled: StateFlow<Boolean> = _stepSensorEnabled.asStateFlow()

    private val _planProvisioningState = MutableStateFlow(PlanProvisioningState())
    val planProvisioningState: StateFlow<PlanProvisioningState> = _planProvisioningState.asStateFlow()

    private val _mediaPackStatus = MutableStateFlow(MediaPackStatus())
    val mediaPackStatus: StateFlow<MediaPackStatus> = _mediaPackStatus.asStateFlow()

    private var stepCounterManager: StepCounterManager? = null
    private var planProvisioningJob: Job? = null
    private var backgroundMediaJob: Job? = null
    private var pendingGifFileNames: List<String> = emptyList()
    private var pendingGoal: FitnessGoal? = null
    private var mobileDataConsentGranted = false

    private val workoutPlanGenerator = WorkoutPlanGenerator(exerciseRepository)

    private fun loadUserProfile() {
        val profile = userPreferences.getUserProfile()
        _userProfile.value = profile
        _completedDays.value = userPreferences.getCompletedDays()
        _startDate.value = userPreferences.getStartDate()
        _weightHistory.value = userPreferences.getWeightHistory()
        refreshHealthMetrics()
        refreshMediaPackStatus()

        // ✅ generatePlan là suspend → cần viewModelScope
        viewModelScope.launch {
            val basePlan = if (profile != null) {
                workoutPlanGenerator.generatePlan(profile.goal)
            } else {
                emptyList()
            }
            _workoutPlan.value = basePlan.map { day ->
                val custom = userPreferences.getCustomDayPlan(day.dayNumber)
                if (custom != null) {
                    day.copy(workoutExercises = remapMissingGifNames(custom))
                } else {
                    day
                }
            }
        }
    }

    fun updateDayPlan(dayNumber: Int, updatedExercises: List<WorkoutExercise>) {
        userPreferences.saveCustomDayPlan(dayNumber, updatedExercises)
        _workoutPlan.value = _workoutPlan.value.map { day ->
            if (day.dayNumber == dayNumber) {
                day.copy(workoutExercises = updatedExercises)
            } else {
                day
            }
        }
    }

    fun saveProfile(
        selectedGoal: FitnessGoal,
        height: Float,
        weight: Float,
        birthYear: Int,
        targetWeight: Float,
        workoutTime: String
    ) {
        userPreferences.saveUserProfile(selectedGoal, height, weight, birthYear, targetWeight, workoutTime)
        userPreferences.setOnboarded(true)
        loadUserProfile()
    }

    fun saveGoal(goal: FitnessGoal) {
        userPreferences.saveGoal(goal)
        loadUserProfile()
    }

    fun startPlanProvisioning(context: Context, goal: FitnessGoal) {
        if (planProvisioningJob?.isActive == true) return

        pendingGoal = goal
        mobileDataConsentGranted = false
        pendingGifFileNames = emptyList()

        _planProvisioningState.value = PlanProvisioningState(
            progress = 0f,
            isCompleted = false,
            isInProgress = true,
            statusMessage = "Creating your plan..."
        )

        planProvisioningJob = viewModelScope.launch {
            try {
                userPreferences.saveGoal(goal)
                _userProfile.value = userPreferences.getUserProfile()

                val generatedPlan = workoutPlanGenerator.generatePlan(goal)
                val mergedPlan = generatedPlan.map { day ->
                    val custom = userPreferences.getCustomDayPlan(day.dayNumber)
                    if (custom != null) {
                        day.copy(workoutExercises = remapMissingGifNames(custom))
                    } else {
                        day
                    }
                }
                _workoutPlan.value = mergedPlan

                _planProvisioningState.value = _planProvisioningState.value.copy(
                    progress = 0.10f,
                    statusMessage = "Preparing resources..."
                )

                val gifFileNames = collectProvisioningGifFileNames(mergedPlan)
                pendingGifFileNames = gifFileNames
                val mediaPackSignature = buildMediaPackSignature(goal, gifFileNames)

                if (userPreferences.isMediaPackReady(mediaPackSignature)) {
                    completePlanProvisioning(mediaPackSignature)
                    syncFullMediaPackInBackground(context.applicationContext)
                    return@launch
                }

                userPreferences.markMediaPackPending(mediaPackSignature, gifFileNames.size)

                if (gifFileNames.isEmpty()) {
                    completePlanProvisioning(mediaPackSignature)
                    syncFullMediaPackInBackground(context.applicationContext)
                    return@launch
                }

                handleDownloadGate(context.applicationContext, goal, gifFileNames)
            } catch (_: Exception) {
                _planProvisioningState.value = _planProvisioningState.value.copy(
                    isInProgress = false,
                    hasError = true,
                    statusMessage = "Unable to finish setup. Please retry."
                )
            }
        }
    }

    fun confirmUseMobileDataForProvisioning(context: Context) {
        if (pendingGifFileNames.isEmpty()) return

        mobileDataConsentGranted = true
        continueDownload(context.applicationContext, pendingGifFileNames)
    }

    fun retryPlanProvisioning(context: Context) {
        if (planProvisioningJob?.isActive == true) return

        _planProvisioningState.value = _planProvisioningState.value.copy(
            hasError = false,
            isInProgress = true,
            statusMessage = "Retrying setup..."
        )

        val failedFiles = userPreferences.getMediaPackFailedFiles()
        if (failedFiles.isNotEmpty()) {
            pendingGifFileNames = failedFiles
            continueDownload(context.applicationContext, failedFiles)
            return
        }

        val fileNames = pendingGifFileNames
        if (fileNames.isNotEmpty()) {
            continueDownload(context.applicationContext, fileNames)
            return
        }

        val goal = pendingGoal ?: _userProfile.value?.goal ?: FitnessGoal.WEIGHT_LOSS
        startPlanProvisioning(context.applicationContext, goal)
    }

    fun markDayComplete(dayNumber: Int) {
        val updated = _completedDays.value + dayNumber
        _completedDays.value = updated
        userPreferences.saveCompletedDays(updated)
    }

    fun recordWeight(weight: Float) {
        userPreferences.recordWeight(weight)
        loadUserProfile()
    }

    fun refreshHealthMetrics() {
        val defaultGoal = defaultWaterGoalMl()
        _todayHealthMetrics.value = userPreferences.getTodayHealthMetrics(defaultGoal)
        _healthMetricsHistory.value = userPreferences.getHealthMetricsHistory()
        _stepSensorEnabled.value = userPreferences.isStepSensorEnabled()
    }

    fun addWater(amountMl: Int) {
        userPreferences.addWater(amountMl, defaultWaterGoalMl())
        refreshHealthMetrics()
    }

    fun setWaterGoal(goalMl: Int) {
        userPreferences.setWaterGoal(goalMl, defaultWaterGoalMl())
        refreshHealthMetrics()
    }

    fun addManualSteps(delta: Int = 500) {
        userPreferences.incrementTodaySteps(delta, defaultWaterGoalMl(), StepSource.MANUAL)
        refreshHealthMetrics()
    }

    fun setActivityRecognitionGranted(granted: Boolean) {
        _activityRecognitionGranted.value = granted
        if (!granted) {
            userPreferences.setStepSensorEnabled(false)
            _stepSensorEnabled.value = false
        }
    }

    fun startStepTracking(context: Context) {
        if (!_activityRecognitionGranted.value) {
            userPreferences.setStepSensorEnabled(false)
            _stepSensorEnabled.value = false
            return
        }

        if (stepCounterManager != null) return

        stepCounterManager = StepCounterManager(context.applicationContext, object : StepCounterManager.Listener {
            override fun onCounterValue(counterValue: Int) {
                val today = LocalDate.now()
                val baselineDay = userPreferences.getStepBaselineDay()
                val baselineValue = userPreferences.getStepBaselineValue()

                val baseline = if (baselineDay == today && baselineValue >= 0) {
                    baselineValue
                } else {
                    userPreferences.setStepBaseline(today, counterValue)
                    counterValue
                }

                val todaySteps = (counterValue - baseline).coerceAtLeast(0)
                userPreferences.setTodaySteps(todaySteps, defaultWaterGoalMl(), StepSource.SENSOR)
                userPreferences.setStepSensorEnabled(true)
                refreshHealthMetrics()
            }

            override fun onStepDetected() {
                userPreferences.incrementTodaySteps(1, defaultWaterGoalMl(), StepSource.SENSOR)
                userPreferences.setStepSensorEnabled(true)
                refreshHealthMetrics()
            }

            override fun onSensorUnavailable() {
                userPreferences.setStepSensorEnabled(false)
                refreshHealthMetrics()
            }
        })

        stepCounterManager?.start()
    }

    fun stopStepTracking() {
        stepCounterManager?.stop()
        stepCounterManager = null
    }

    fun downloadFullMediaPackOnDemand(context: Context) {
        syncFullMediaPackInBackground(context.applicationContext, allowCellular = true)
    }

    fun downloadAllLibraryMediaOnDemand(context: Context) {
        if (backgroundMediaJob?.isActive == true) return

        backgroundMediaJob = viewModelScope.launch {
            try {
                val allLibraryNames = allExercises.value
                    .asSequence()
                    .flatMap { it.local_gifs.asSequence() }
                    .map { GifUrlHelper.canonicalize(it) }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .toList()

                if (allLibraryNames.isEmpty()) return@launch

                _mediaPackStatus.value = _mediaPackStatus.value.copy(
                    isSyncing = true,
                    total = allLibraryNames.size
                )

                val manager = GifDownloadManager(context.applicationContext)
                val result = manager.downloadMissing(allLibraryNames) { done, total ->
                    userPreferences.updateMediaPackProgress(done, total)
                    _mediaPackStatus.value = _mediaPackStatus.value.copy(
                        downloaded = done,
                        total = total,
                        isSyncing = true
                    )
                }

                userPreferences.updateMediaPackProgress(result.downloaded, result.total)
                userPreferences.setMediaPackFailedFiles(result.failedFiles)
            } catch (error: Exception) {
                Log.w("MediaPack", "Manual full-library download failed", error)
            } finally {
                _mediaPackStatus.value = _mediaPackStatus.value.copy(isSyncing = false)
                refreshMediaPackStatus()
            }
        }
    }

    fun syncFullMediaPackInBackground(context: Context, allowCellular: Boolean = false) {
        if (backgroundMediaJob?.isActive == true) return

        val goal = _userProfile.value?.goal ?: return
        val network = NetworkStateHelper.getNetworkState(context.applicationContext)
        if (!network.isConnected) return
        if (network.isCellular && !allowCellular) return

        backgroundMediaJob = viewModelScope.launch {
            try {
                val allNames = collectAllGifFileNames(_workoutPlan.value)
                if (allNames.isEmpty()) return@launch

                val signature = buildMediaPackSignature(goal, allNames)
                if (userPreferences.isMediaPackReady(signature)) {
                    refreshMediaPackStatus()
                    return@launch
                }

                _mediaPackStatus.value = _mediaPackStatus.value.copy(
                    isSyncing = true,
                    total = allNames.size
                )

                val manager = GifDownloadManager(context.applicationContext)
                val result = manager.downloadMissing(allNames) { done, total ->
                    userPreferences.updateMediaPackProgress(done, total)
                    _mediaPackStatus.value = _mediaPackStatus.value.copy(
                        downloaded = done,
                        total = total,
                        isSyncing = true
                    )
                }

                if (result.failedFiles.isEmpty()) {
                    userPreferences.markMediaPackReady(signature, result.downloaded, result.total)
                } else {
                    userPreferences.markMediaPackPending(signature, result.total, result.failedFiles)
                    userPreferences.updateMediaPackProgress(result.downloaded, result.total)
                    userPreferences.setMediaPackFailedFiles(result.failedFiles)
                }
            } catch (error: Exception) {
                Log.w("MediaPack", "Background sync failed", error)
            } finally {
                _mediaPackStatus.value = _mediaPackStatus.value.copy(isSyncing = false)
                refreshMediaPackStatus()
            }
        }
    }

    fun resetLocalMediaPackForDebug(context: Context) {
        val dir = File(context.applicationContext.filesDir, "gifs")
        if (dir.exists()) {
            dir.listFiles()?.forEach { file ->
                runCatching { file.delete() }
            }
        }

        userPreferences.markMediaPackPending("debug-reset", 0)
        userPreferences.updateMediaPackProgress(0, 0)
        userPreferences.setMediaPackFailedFiles(emptyList())
        refreshMediaPackStatus()
    }

    fun scheduleWorkoutReminder(context: Context, hour: Int, minute: Int) {
        Log.d("FitFlowDebug", "Scheduling alarm for $hour:$minute")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WorkoutReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        // Lặp lại hàng ngày
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    init {
        loadUserProfile()
    }

    private fun refreshMediaPackStatus() {
        val libraryTotal = allExercises.value
            .asSequence()
            .flatMap { it.local_gifs.asSequence() }
            .map { GifUrlHelper.canonicalize(it) }
            .filter { it.isNotBlank() }
            .distinct()
            .count()

        val persistedDownloaded = userPreferences.getMediaPackDownloadedCount()
        val downloaded = persistedDownloaded.coerceIn(0, libraryTotal)

        _mediaPackStatus.value = MediaPackStatus(
            isReady = userPreferences.isMediaPackReady(),
            downloaded = downloaded,
            total = libraryTotal,
            failedCount = userPreferences.getMediaPackFailedFiles().size,
            isSyncing = backgroundMediaJob?.isActive == true
        )
    }

    private fun defaultWaterGoalMl(): Int {
        val weight = _userProfile.value?.weight ?: 57f
        return (weight * 35f).toInt().coerceIn(1200, 5000)
    }

    private suspend fun handleDownloadGate(
        context: Context,
        goal: FitnessGoal,
        gifFileNames: List<String>
    ) {
        val network = NetworkStateHelper.getNetworkState(context)

        when {
            !network.isConnected -> {
                _planProvisioningState.value = _planProvisioningState.value.copy(
                    isInProgress = false,
                    isNoNetwork = true,
                    requiresMobileDataConsent = false,
                    statusMessage = "Connect to Wi-Fi or mobile data to continue setup."
                )
            }

            network.isCellular && !mobileDataConsentGranted -> {
                _planProvisioningState.value = _planProvisioningState.value.copy(
                    isInProgress = false,
                    isNoNetwork = false,
                    requiresMobileDataConsent = true,
                    statusMessage = "Using mobile data may consume data. Continue?"
                )
            }

            else -> {
                runDownload(context, buildMediaPackSignature(goal, gifFileNames), gifFileNames)
            }
        }
    }

    private fun continueDownload(context: Context, fileNames: List<String>) {
        val goal = pendingGoal ?: _userProfile.value?.goal ?: FitnessGoal.WEIGHT_LOSS

        planProvisioningJob = viewModelScope.launch {
            try {
                handleDownloadGate(context, goal, fileNames)
            } catch (error: Exception) {
                Log.e("PlanProvisioning", "continueDownload failed", error)
                _planProvisioningState.value = _planProvisioningState.value.copy(
                    isInProgress = false,
                    hasError = true,
                    statusMessage = "Unable to finish setup. Please retry."
                )
            }
        }
    }

    private suspend fun runDownload(
        context: Context,
        goalSignature: String,
        fileNames: List<String>
    ) {
        _planProvisioningState.value = _planProvisioningState.value.copy(
            isInProgress = true,
            isNoNetwork = false,
            requiresMobileDataConsent = false,
            hasError = false,
            progress = 0.10f,
            statusMessage = "Downloading workout media..."
        )

        val downloadManager = GifDownloadManager(context.applicationContext)
        val result = downloadManager.downloadMissing(fileNames) { done, total ->
            val downloadProgress = done / total.toFloat()
            val weightedProgress = 0.10f + (downloadProgress * 0.90f)
            _planProvisioningState.value = _planProvisioningState.value.copy(progress = weightedProgress)
            userPreferences.updateMediaPackProgress(done, total)
        }

        if (result.failedFiles.isNotEmpty()) {
            Log.w("PlanProvisioning", "Some GIF downloads failed: ${result.failedFiles.size}/${result.total}")
            userPreferences.markMediaPackPending(goalSignature, result.total, result.failedFiles)
            userPreferences.updateMediaPackProgress(result.downloaded, result.total)
            userPreferences.setMediaPackFailedFiles(result.failedFiles)
        } else {
            userPreferences.markMediaPackReady(goalSignature, result.downloaded, result.total)
        }

        completePlanProvisioning(goalSignature)
        syncFullMediaPackInBackground(context.applicationContext)
    }

    private fun completePlanProvisioning(goalSignature: String) {
        userPreferences.markHybridGifCacheReady(goalSignature)
        pendingGifFileNames = emptyList()
        refreshMediaPackStatus()

        _planProvisioningState.value = _planProvisioningState.value.copy(
            progress = 1f,
            isInProgress = false,
            isCompleted = true,
            isNoNetwork = false,
            requiresMobileDataConsent = false,
            hasError = false,
            statusMessage = "Setup complete"
        )
    }

    private fun collectProvisioningGifFileNames(plan: List<DayPlan>): List<String> {
        return plan
            .asSequence()
            .filter { !it.isRest }
            .take(7)
            .flatMap { it.workoutExercises.asSequence() }
            .mapNotNull { exercise ->
                exercise.gifFileName
                    .takeIf { it.isNotEmpty() }
            }
            .distinct()
            .toList()
    }

    private fun collectAllGifFileNames(plan: List<DayPlan>): List<String> {
        return plan
            .asSequence()
            .filter { !it.isRest }
            .flatMap { it.workoutExercises.asSequence() }
            .mapNotNull { exercise ->
                exercise.gifFileName.takeIf { it.isNotEmpty() }
            }
            .distinct()
            .toList()
    }

    private fun buildMediaPackSignature(goal: FitnessGoal, fileNames: List<String>): String {
        val versionPart = fileNames
            .asSequence()
            .map { GifUrlHelper.getVersion(it) }
            .distinct()
            .sorted()
            .joinToString("+")
            .ifEmpty { "none" }

        return "${goal.name}|$versionPart|${fileNames.sorted().joinToString(",").hashCode()}"
    }

    private suspend fun remapMissingGifNames(exercises: List<WorkoutExercise>): List<WorkoutExercise> {
        return exercises.map { exercise ->
            if (exercise.gifFileName.isNotBlank()) {
                exercise
            } else {
                val mapped = exerciseRepository.getGifFileName(exercise.name).orEmpty()
                if (mapped.isBlank()) {
                    exercise
                } else {
                    exercise.copy(gifFileName = mapped)
                }
            }
        }
    }
}

class UserViewModelFactory(
    private val userPreferences: UserPreferences,
    private val exerciseRepository: ExerciseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userPreferences, exerciseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}