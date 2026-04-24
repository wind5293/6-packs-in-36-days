package com.example.fitflow.domain

import com.example.fitflow.data.model.BmiCategory

fun calculateBmi(height: Float, weight: Float): Float {
    return weight / ((height / 100) * (height / 100))
}

fun getBmiCategory(bmi: Float): BmiCategory {
    return when {
        bmi < 18.5f -> return BmiCategory.UNDERWEIGHT
        bmi < 25.0f -> return BmiCategory.NORMAL
        else -> return BmiCategory.OVERWEIGHT
    }
}