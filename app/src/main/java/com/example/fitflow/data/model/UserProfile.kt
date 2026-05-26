package com.example.fitflow.data.model

enum class BmiCategory {
    UNDERWEIGHT,
    NORMAL,
    OVERWEIGHT,
}

enum class FitnessGoal(val title: String, val description: String) {
    WEIGHT_LOSS("Weight Loss", "Burn fat and get leaner"),
    MUSCLE_GAIN("Muscle Gain", "Build strength and volume"),
    ENDURANCE("Endurance", "Improve stamina and cardio"),
    MAINTENANCE("Maintenance", "Stay fit and healthy"),
    ABS_CORE_STRENGTH("Abs & Core Strength", "Improve abs and core strength")
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
