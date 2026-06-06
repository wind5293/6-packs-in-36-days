package com.example.fitflow.data.model

data class WorkoutLogEntry(
    val dateEpochDay: Long,
    val timestampMillis: Long,
    val dayNumber: Int,
    val goalName: String,
    val durationSec: Int,
    val kcal: Int,
    val isRest: Boolean
)
