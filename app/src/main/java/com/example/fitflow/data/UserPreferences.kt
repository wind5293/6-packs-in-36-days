package com.example.fitflow.data

import android.content.Context
import com.example.fitflow.data.model.UserProfile
import com.example.fitflow.domain.calculateBmi
import com.example.fitflow.domain.getBmiCategory

class UserPreferences (context: Context) {
    private val prefs = context.getSharedPreferences("fitflow_prefs", Context.MODE_PRIVATE)
    companion object {
        const val KEY_HEIGHT        = "height"
        const val KEY_WEIGHT        = "weight"
        const val KEY_IS_ONBOARDED  = "is_onboarded"
        const val KEY_COMPLETED_DAYS = "completed_days"
    }

    fun saveUserProfile(height: Float, weight: Float) {
        prefs.edit()
            .putFloat(KEY_HEIGHT, height)
            .putFloat(KEY_WEIGHT, weight)
            .apply()
    }

    fun getUserProfile(): UserProfile? {
        val height = prefs.getFloat(KEY_HEIGHT, 0f)
        val weight = prefs.getFloat(KEY_WEIGHT, 0f)

        if (height == 0f || weight == 0f) {
            return null
        }

        val bmi = calculateBmi(height, weight)
        val bmiCategory = getBmiCategory(bmi)

        return UserProfile(height, weight, bmi, bmiCategory)
    }

    fun setOnboarded(value: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ONBOARDED, value).apply()
    }

    fun isOnboarded(): Boolean = prefs.getBoolean(KEY_IS_ONBOARDED, false)

    fun saveCompletedDays(days: Set<Int>) {
        prefs.edit().putString(KEY_COMPLETED_DAYS, days.joinToString(",")).apply()
    }

    fun getCompletedDays(): Set<Int> {
        val raw = prefs.getString(KEY_COMPLETED_DAYS, "") ?: return emptySet()
        if (raw.isEmpty()) return emptySet()
        return raw.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }
}