package com.example.fitflow.data.model

enum class BmiCategory {
    UNDERWEIGHT,
    NORMAL,
    OVERWEIGHT,
}

data class UserProfile(
    val height: Float,
    val weight: Float,
    val bmi: Float,
    val bmiCategory: BmiCategory,
)