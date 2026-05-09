package com.example.fitflow.data.model

enum class BmiCategory {
    UNDERWEIGHT,
    NORMAL,
    OVERWEIGHT,
}

enum class FitnessGoal {
    WEIGHT_LOSS,
    MUSCLE_GAIN,
    ENDURANCE,
    MAINTENANCE
}

data class UserProfile(
    val height: Float,
    val weight: Float,
    val targetWeight: Float,
    val bmi: Float,
    val bmiCategory: BmiCategory,
    val goal: FitnessGoal = FitnessGoal.WEIGHT_LOSS
)
