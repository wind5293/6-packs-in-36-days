package com.example.fitflow.data.model

enum class BmiCategory {
    UNDERWEIGHT,
    NORMAL,
    OVERWEIGHT,
}

enum class FitnessGoal(val title: String, val description: String) {
    WEIGHT_LOSS("Weight Loss", "Cardio-focused · Burn calories"),
    MUSCLE_GAIN("Muscle Gain", "Strength-focused · Build muscle"),
    ENDURANCE("Endurance", "Mixed training · Increase stamina"),
    MAINTENANCE("Maintenance", "Balanced workout · Stay fit")
}

data class UserProfile(
    val height: Float,
    val weight: Float,
    val birthYear: Int,
    val targetWeight: Float,
    val bmi: Float,
    val bmiCategory: BmiCategory,
    val goal: FitnessGoal,
    val equipment: String = "bodyweight",
)
