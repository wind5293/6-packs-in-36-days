package com.example.fitflow.data.model

data class DayPlan(
    val dayNumber: Int,
    val isRest: Boolean,
    val exercises: List<Exercise>
) {
    val day: Int get() = dayNumber
}