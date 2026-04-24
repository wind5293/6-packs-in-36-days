package com.example.fitflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitflow.data.UserPreferences
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.UserProfile
import com.example.fitflow.domain.WorkoutPlangenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel(private val userPreferences: UserPreferences) : ViewModel() {
    private val _userProfile: MutableStateFlow<UserProfile?> = MutableStateFlow(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _workoutPlan = MutableStateFlow<List<DayPlan>>(emptyList())
    val workoutPlan: StateFlow<List<DayPlan>> = _workoutPlan.asStateFlow()

    private val _completedDays = MutableStateFlow<Set<Int>>(emptySet())
    val completedDays: StateFlow<Set<Int>> = _completedDays.asStateFlow()

    private fun loadUserProfile() {
        val profile = userPreferences.getUserProfile()
        _userProfile.value = profile
        _completedDays.value = userPreferences.getCompletedDays()
        _workoutPlan.value = if (profile != null) {
            WorkoutPlangenerator.generatePlan(profile.bmiCategory)
        } else {
            emptyList()
        }
    }

    fun saveProfile(height: Float, weight: Float) {
        userPreferences.saveUserProfile(height, weight)
        userPreferences.setOnboarded(true)
        loadUserProfile()
    }

    fun markDayComplete(dayNumber: Int) {
        val updated = _completedDays.value + dayNumber
        _completedDays.value = updated
        userPreferences.saveCompletedDays(updated)
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