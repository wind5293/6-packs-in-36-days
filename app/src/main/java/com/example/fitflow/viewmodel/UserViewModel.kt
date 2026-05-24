package com.example.fitflow.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
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

class UserViewModel(
    private val userPreferences: UserPreferences,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    private val _userProfile: MutableStateFlow<UserProfile?> = MutableStateFlow(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

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

    private var stepCounterManager: StepCounterManager? = null

    private val workoutPlanGenerator = WorkoutPlanGenerator(exerciseRepository)

    private fun loadUserProfile() {
        val profile = userPreferences.getUserProfile()
        _userProfile.value = profile
        _completedDays.value = userPreferences.getCompletedDays()
        _startDate.value = userPreferences.getStartDate()
        _weightHistory.value = userPreferences.getWeightHistory()
        refreshHealthMetrics()

        // ✅ generatePlan là suspend → cần viewModelScope
        viewModelScope.launch {
            _workoutPlan.value = if (profile != null) {
                workoutPlanGenerator.generatePlan(profile.goal)
            } else {
                emptyList()
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