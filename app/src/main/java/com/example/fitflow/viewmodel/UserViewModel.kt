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
import com.example.fitflow.data.UserPreferences
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.FitnessGoal
import com.example.fitflow.data.model.UserProfile
import com.example.fitflow.domain.WorkoutPlangenerator
import com.example.fitflow.notification.WorkoutReminderReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class UserViewModel(private val userPreferences: UserPreferences) : ViewModel() {
    private val _userProfile: MutableStateFlow<UserProfile?> = MutableStateFlow(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _workoutPlan = MutableStateFlow<List<DayPlan>>(emptyList())
    val workoutPlan: StateFlow<List<DayPlan>> = _workoutPlan.asStateFlow()

    private val _completedDays = MutableStateFlow<Set<Int>>(emptySet())
    val completedDays: StateFlow<Set<Int>> = _completedDays.asStateFlow()

    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate.asStateFlow()

    private fun loadUserProfile() {
        val profile = userPreferences.getUserProfile()
        _userProfile.value = profile
        _completedDays.value = userPreferences.getCompletedDays()
        _startDate.value = userPreferences.getStartDate()
        _workoutPlan.value = if (profile != null) {
            WorkoutPlangenerator.generatePlan(profile.goal)
        } else {
            emptyList()
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
}

class UserViewModelFactory(private val userPreferences: UserPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}