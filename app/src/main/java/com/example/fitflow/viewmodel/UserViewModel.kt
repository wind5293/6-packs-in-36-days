package com.example.fitflow.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import coil.request.CachePolicy
import coil.request.ImageRequest
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitflow.data.ExerciseRepository
import com.example.fitflow.data.StepCounterManager
import com.example.fitflow.data.UserPreferences
import com.example.fitflow.data.PlanRepository
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.DailyHealthMetrics
import com.example.fitflow.data.model.FitnessGoal
import com.example.fitflow.data.model.StepSource
import com.example.fitflow.data.model.UserProfile
import com.example.fitflow.data.model.WorkoutExercise
import com.example.fitflow.data.model.WorkoutLogEntry
import com.example.fitflow.domain.WorkoutPlanGenerator
import com.example.fitflow.notification.WorkoutReminderReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.example.fitflow.data.model.Exercise
import com.example.fitflow.utils.GifUrlHelper
import com.example.fitflow.utils.NetworkStateHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PlanProvisioningState(
    val firstSegmentProgress: Float = 0f,
    val secondSegmentProgress: Float = 0f,
    val isCompleted: Boolean = false,
    val requiresMobileDataConsent: Boolean = false,
    val isNoNetwork: Boolean = false,
    val isInProgress: Boolean = false,
    val hasError: Boolean = false,
    val statusMessage: String = "Creating your plan..."
)

class UserViewModel(
    private val userPreferences: UserPreferences,
    private val exerciseRepository: ExerciseRepository,
    private val planRepository: PlanRepository
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

    private val _completedDateMap = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val completedDateMap: StateFlow<Map<LocalDate, Int>> = _completedDateMap.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _longestStreak = MutableStateFlow(0)
    val longestStreak: StateFlow<Int> = _longestStreak.asStateFlow()

    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate.asStateFlow()

    private val _weightHistory = MutableStateFlow<List<Pair<LocalDate, Float>>>(emptyList())
    val weightHistory: StateFlow<List<Pair<LocalDate, Float>>> = _weightHistory.asStateFlow()

    // dayNumber -> epochMillis of when the day was completed
    private val _workoutTimestamps = MutableStateFlow<Map<Int, Long>>(emptyMap())
    val workoutTimestamps: StateFlow<Map<Int, Long>> = _workoutTimestamps.asStateFlow()

    private val _todayHealthMetrics = MutableStateFlow(
        DailyHealthMetrics(
            date = LocalDate.now(),
            steps = 0,
            waterIntakeMl = 0,
            waterGoalMl = 2000,
            stepGoal = 6000,
            stepSource = StepSource.MANUAL
        )
    )
    val todayHealthMetrics: StateFlow<DailyHealthMetrics> = _todayHealthMetrics.asStateFlow()

    private val _healthMetricsHistory = MutableStateFlow<List<DailyHealthMetrics>>(emptyList())
    val healthMetricsHistory: StateFlow<List<DailyHealthMetrics>> = _healthMetricsHistory.asStateFlow()

    private val _globalWorkoutLogs = MutableStateFlow<List<WorkoutLogEntry>>(emptyList())
    val globalWorkoutLogs: StateFlow<List<WorkoutLogEntry>> = _globalWorkoutLogs.asStateFlow()

    private val _activityRecognitionGranted = MutableStateFlow(false)
    val activityRecognitionGranted: StateFlow<Boolean> = _activityRecognitionGranted.asStateFlow()

    private val _stepSensorEnabled = MutableStateFlow(false)
    val stepSensorEnabled: StateFlow<Boolean> = _stepSensorEnabled.asStateFlow()

    private val _stepTrackingActive = MutableStateFlow(false)
    val stepTrackingActive: StateFlow<Boolean> = _stepTrackingActive.asStateFlow()

    private val _planProvisioningState = MutableStateFlow(PlanProvisioningState())
    val planProvisioningState: StateFlow<PlanProvisioningState> = _planProvisioningState.asStateFlow()

    private var stepCounterManager: StepCounterManager? = null
    private var planProvisioningJob: Job? = null
    private var pendingHybridUrls: List<String> = emptyList()
    private var pendingGoal: FitnessGoal? = null
    private var mobileDataConsentGranted = false

    private val workoutPlanGenerator = WorkoutPlanGenerator(exerciseRepository, planRepository)

    private fun loadUserProfile() {
        val profile = userPreferences.getUserProfile()
        _userProfile.value = profile
        // Load progress scoped to the active goal
        val activeGoal = profile?.goal
        _completedDays.value = if (activeGoal != null)
            userPreferences.getCompletedDaysForGoal(activeGoal)
        else
            userPreferences.getCompletedDays() // fallback (first-run / no goal yet)
        _completedDateMap.value = if (activeGoal != null)
            userPreferences.getCompletedDateMapForGoal(activeGoal)
        else
            userPreferences.getCompletedDateMap()
        _currentStreak.value = userPreferences.getCurrentStreak()
        _longestStreak.value = userPreferences.getLongestStreak()
        _startDate.value = userPreferences.getStartDate()
        _weightHistory.value = userPreferences.getWeightHistory()
        _workoutTimestamps.value = userPreferences.getWorkoutTimestamps()
        _globalWorkoutLogs.value = userPreferences.getGlobalWorkoutLogs()
        refreshHealthMetrics()

        viewModelScope.launch {
            val basePlan = if (profile != null) {
                workoutPlanGenerator.generatePlan(profile.goal)
            } else {
                emptyList()
            }
            _workoutPlan.value = basePlan.map { day ->
                val custom = userPreferences.getCustomDayPlan(day.dayNumber)
                if (custom != null) day.copy(workoutExercises = custom) else day
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

    suspend fun getSupplementaryWorkout(id: String): com.example.fitflow.data.model.SupplementaryWorkout? {
        return com.example.fitflow.domain.PushYourLimitsCatalog.findEnrichedById(id, exerciseRepository)
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
        pendingHybridUrls = emptyList()
        userPreferences.markHybridGifCachePending(goal.name)

        _planProvisioningState.value = PlanProvisioningState(
            firstSegmentProgress = 0f,
            secondSegmentProgress = 0f,
            isCompleted = false,
            isInProgress = true,
            statusMessage = "Creating your plan..."
        )

        planProvisioningJob = viewModelScope.launch {
            try {
                userPreferences.saveGoal(goal)
                userPreferences.clearCustomDayPlans()
                _userProfile.value = userPreferences.getUserProfile()
                // Immediately sync progress state to the NEW goal so UI is correct
                _completedDays.value = userPreferences.getCompletedDaysForGoal(goal)
                _completedDateMap.value = userPreferences.getCompletedDateMapForGoal(goal)

                val generatedPlan = workoutPlanGenerator.generatePlan(goal)
                val mergedPlan = generatedPlan.map { day ->
                    val custom = userPreferences.getCustomDayPlan(day.dayNumber)
                    if (custom != null) day.copy(workoutExercises = custom) else day
                }
                _workoutPlan.value = mergedPlan

                _planProvisioningState.value = _planProvisioningState.value.copy(
                    firstSegmentProgress = 1f,
                    statusMessage = "Preparing resources..."
                )

                val hybridUrls = collectHybridGifUrls(mergedPlan)
                pendingHybridUrls = hybridUrls

                if (hybridUrls.isEmpty()) {
                    completePlanProvisioning(goal.name)
                    return@launch
                }

                handlePrefetchGate(context.applicationContext, goal, hybridUrls)
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
        if (pendingHybridUrls.isEmpty()) return

        mobileDataConsentGranted = true
        continuePrefetch(context.applicationContext, pendingHybridUrls)
    }

    fun retryPlanProvisioning(context: Context) {
        if (planProvisioningJob?.isActive == true) return

        _planProvisioningState.value = _planProvisioningState.value.copy(
            hasError = false,
            isInProgress = true,
            statusMessage = "Retrying setup..."
        )

        val urls = pendingHybridUrls
        if (urls.isNotEmpty()) {
            continuePrefetch(context.applicationContext, urls)
            return
        }

        val goal = pendingGoal ?: _userProfile.value?.goal ?: FitnessGoal.WEIGHT_LOSS
        startPlanProvisioning(context.applicationContext, goal)
    }

    fun markDayComplete(dayNumber: Int): Boolean {
        var isNewStreak = false
        val current = _completedDays.value.toMutableSet()
        if (!current.contains(dayNumber)) {
            current.add(dayNumber)
            val activeGoal = _userProfile.value?.goal
            if (activeGoal != null) {
                userPreferences.saveCompletedDaysForGoal(activeGoal, current)
            } else {
                userPreferences.saveCompletedDays(current)
            }
            _completedDays.value = current

            val dateMap = _completedDateMap.value.toMutableMap()
            dateMap[LocalDate.now()] = dayNumber
            if (activeGoal != null) {
                userPreferences.saveCompletedDateMapForGoal(activeGoal, dateMap)
            } else {
                userPreferences.saveCompletedDateMap(dateMap)
            }
            _completedDateMap.value = dateMap

            // Save exact timestamp
            val nowMillis = System.currentTimeMillis()
            userPreferences.saveWorkoutTimestamp(dayNumber, nowMillis)
            _workoutTimestamps.value = _workoutTimestamps.value.toMutableMap().also { it[dayNumber] = nowMillis }

            val today = LocalDate.now()
            
            // Save global workout log
            val plan = _workoutPlan.value.find { it.dayNumber == dayNumber }
            val durationSec = plan?.workoutExercises?.sumOf { it.durationSec } ?: 0
            val kcal = plan?.workoutExercises?.sumOf { it.kcal } ?: 0
            val isRest = plan?.isRest ?: false
            val goalName = activeGoal?.name ?: "UNKNOWN"
            val logEntry = WorkoutLogEntry(
                dateEpochDay = today.toEpochDay(),
                timestampMillis = nowMillis,
                dayNumber = dayNumber,
                goalName = goalName,
                durationSec = durationSec,
                kcal = kcal,
                isRest = isRest
            )
            userPreferences.addGlobalWorkoutLog(logEntry)
            _globalWorkoutLogs.value = _globalWorkoutLogs.value + logEntry

            val lastWorkout = userPreferences.getLastWorkoutDate()
            var streak = userPreferences.getCurrentStreak()

            if (lastWorkout == null) {
                streak = 1
            } else {
                val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastWorkout, today)
                if (daysBetween == 1L) {
                    streak += 1
                } else if (daysBetween > 1L) {
                    streak = 1
                }
            }

            if (lastWorkout != today) {
                isNewStreak = true
                userPreferences.setCurrentStreak(streak)
                userPreferences.setLastWorkoutDate(today)
                _currentStreak.value = streak

                // Cập nhật kỷ lục dài nhất nếu streak mới hơn
                val longest = userPreferences.getLongestStreak()
                if (streak > longest) {
                    userPreferences.setLongestStreak(streak)
                    _longestStreak.value = streak
                }
            }
        }
        return isNewStreak
    }

    /**
     * Reset progress for the currently active goal back to Day 1.
     * All completed-day data for this goal is cleared.
     */
    fun resetPlan() {
        val activeGoal = _userProfile.value?.goal ?: return
        userPreferences.clearCompletedDaysForGoal(activeGoal)
        _completedDays.value = emptySet()
        _completedDateMap.value = emptyMap()
    }

    /**
     * How many workout days the user has completed for the given goal.
     * Used to decide whether to show the "Resume?" dialog in UpdateGoalScreen.
     */
    fun getCompletedCountForGoal(goal: FitnessGoal): Int =
        userPreferences.getCompletedCountForGoal(goal)

    /**
     * Explicitly wipe the frozen progress for a given goal.
     * Called from MainActivity when the user chooses "Day 1" in the resume dialog.
     */
    fun clearProgressForGoal(goal: FitnessGoal) {
        userPreferences.clearCompletedDaysForGoal(goal)
        // If we're clearing the currently-active goal, update state too
        if (_userProfile.value?.goal == goal) {
            _completedDays.value = emptySet()
            _completedDateMap.value = emptyMap()
        }
    }

    fun recordWeight(weight: Float) {
        userPreferences.recordWeight(weight)
        loadUserProfile()
    }

    fun refreshHealthMetrics() {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            val defaultGoal = defaultWaterGoalMl()
            _todayHealthMetrics.value = userPreferences.getTodayHealthMetrics(defaultGoal)
            _healthMetricsHistory.value = userPreferences.getHealthMetricsHistory()
            _stepSensorEnabled.value = userPreferences.isStepSensorEnabled()
        }
    }

    fun addWater(amountMl: Int) {
        userPreferences.addWater(amountMl, defaultWaterGoalMl())
        refreshHealthMetrics()
    }

    fun setWaterGoal(goalMl: Int) {
        userPreferences.setWaterGoal(goalMl, defaultWaterGoalMl())
        refreshHealthMetrics()
    }

    fun setStepGoal(goalSteps: Int) {
        userPreferences.setStepGoal(goalSteps, defaultWaterGoalMl())
        refreshHealthMetrics()
    }

    fun addManualSteps(delta: Int = 500) {
        userPreferences.incrementTodaySteps(delta, defaultWaterGoalMl(), StepSource.MANUAL)
        refreshHealthMetrics()
    }

    fun setActivityRecognitionGranted(granted: Boolean) {
        _activityRecognitionGranted.value = granted
        if (!granted) {
            stopStepTracking()
            userPreferences.setStepSensorEnabled(false)
            _stepSensorEnabled.value = false
        }
    }

    fun startStepTracking(context: Context) {
        if (!_activityRecognitionGranted.value) {
            userPreferences.setStepSensorEnabled(false)
            _stepSensorEnabled.value = false
            _stepTrackingActive.value = false
            return
        }

        if (stepCounterManager != null) {
            _stepTrackingActive.value = true
            return
        }

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
                _stepTrackingActive.value = false
                userPreferences.setStepSensorEnabled(false)
                refreshHealthMetrics()
            }
        })

        val started = stepCounterManager?.start() == true
        _stepTrackingActive.value = started
        _stepSensorEnabled.value = started
        userPreferences.setStepSensorEnabled(started)
        if (!started) {
            stepCounterManager = null
        }
    }

    fun stopStepTracking() {
        stepCounterManager?.stop()
        stepCounterManager = null
        _stepTrackingActive.value = false
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

    fun scheduleDemoWorkoutReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WorkoutReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 1001, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set alarm for 5 seconds from now
        val triggerTime = System.currentTimeMillis() + 5000
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    init {
        loadUserProfile()
    }

    private fun defaultWaterGoalMl(): Int {
        val weight = _userProfile.value?.weight ?: 57f
        return (weight * 35f).toInt().coerceIn(1200, 5000)
    }

    private suspend fun handlePrefetchGate(
        context: Context,
        goal: FitnessGoal,
        hybridUrls: List<String>
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
                runPrefetch(context, goal.name, hybridUrls)
            }
        }
    }

    private fun continuePrefetch(context: Context, urls: List<String>) {
        val goal = pendingGoal ?: _userProfile.value?.goal ?: FitnessGoal.WEIGHT_LOSS

        planProvisioningJob = viewModelScope.launch {
            try {
                handlePrefetchGate(context, goal, urls)
            } catch (error: Exception) {
                Log.e("PlanProvisioning", "continuePrefetch failed", error)
                _planProvisioningState.value = _planProvisioningState.value.copy(
                    isInProgress = false,
                    hasError = true,
                    statusMessage = "Unable to finish setup. Please retry."
                )
            }
        }
    }

    private suspend fun runPrefetch(
        context: Context,
        goalSignature: String,
        urls: List<String>
    ) {
        _planProvisioningState.value = _planProvisioningState.value.copy(
            isInProgress = true,
            isNoNetwork = false,
            requiresMobileDataConsent = false,
            hasError = false,
            secondSegmentProgress = 0f,
            statusMessage = "Finalizing setup..."
        )

        val imageLoader = (context.applicationContext as com.example.fitflow.FitFlowApplication).imageLoader
        val total = urls.size.coerceAtLeast(1)

        withContext(Dispatchers.IO) {
            urls.forEachIndexed { index, url ->
                try {
                    imageLoader.execute(
                        ImageRequest.Builder(context)
                            .data(url)
                            .memoryCacheKey(url)
                            .diskCacheKey(url)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build()
                    )
                } catch (error: Exception) {
                    Log.w("PlanProvisioning", "Prefetch failed for $url", error)
                    // Skip failed URL and continue completion accounting.
                }

                val progress = (index + 1) / total.toFloat()
                _planProvisioningState.value = _planProvisioningState.value.copy(secondSegmentProgress = progress)
            }
        }

        completePlanProvisioning(goalSignature)
    }

    private fun completePlanProvisioning(goalSignature: String) {
        userPreferences.markHybridGifCacheReady(goalSignature)
        pendingHybridUrls = emptyList()
        // Reload full profile & progress so the dashboard reflects the NEW goal
        loadUserProfile()

        _planProvisioningState.value = _planProvisioningState.value.copy(
            firstSegmentProgress = 1f,
            secondSegmentProgress = 1f,
            isInProgress = false,
            isCompleted = true,
            isNoNetwork = false,
            requiresMobileDataConsent = false,
            hasError = false,
            statusMessage = "Setup complete"
        )
    }

    private fun collectHybridGifUrls(plan: List<DayPlan>): List<String> {
        return plan
            .asSequence()
            .filter { !it.isRest }
            .take(7)
            .flatMap { it.workoutExercises.asSequence() }
            .mapNotNull { exercise ->
                exercise.gifFileName
                    .takeIf { it.isNotEmpty() }
                    ?.let { GifUrlHelper.getUrl(it) }
            }
            .distinct()
            .toList()
    }
}

class UserViewModelFactory(
    private val userPreferences: UserPreferences,
    private val exerciseRepository: ExerciseRepository,
    private val planRepository: PlanRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userPreferences, exerciseRepository, planRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}