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
import com.example.fitflow.domain.WorkoutPlanGenerator
import com.example.fitflow.notification.WorkoutReminderReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

import com.example.fitflow.data.model.Exercise
import com.example.fitflow.data.model.WorkoutExercise
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

    private var stepCounterManager: StepCounterManager? = null
    private var planProvisioningJob: Job? = null
    private var pendingHybridUrls: List<String> = emptyList()
    private var pendingGoal: FitnessGoal? = null
    private var mobileDataConsentGranted = false

    private val workoutPlanGenerator = WorkoutPlanGenerator(exerciseRepository, planRepository)

    private fun loadUserProfile() {
        val profile = userPreferences.getUserProfile()
        _userProfile.value = profile
        _completedDays.value = userPreferences.getCompletedDays()
        _startDate.value = userPreferences.getStartDate()
        _weightHistory.value = userPreferences.getWeightHistory()
        refreshHealthMetrics()

        // ✅ generatePlan là suspend → cần viewModelScope
        viewModelScope.launch {
            val basePlan = if (profile != null) {
                workoutPlanGenerator.generatePlan(profile.goal, profile.equipment)
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

    fun startPlanProvisioning(context: Context, goal: FitnessGoal, equipment: String = "bodyweight") {
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
                userPreferences.saveEquipment(equipment)
                userPreferences.clearCustomDayPlans()
                _userProfile.value = userPreferences.getUserProfile()

                val generatedPlan = workoutPlanGenerator.generatePlan(goal, equipment)
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
        val equipment = _userProfile.value?.equipment ?: "bodyweight"
        startPlanProvisioning(context.applicationContext, goal, equipment)
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