package com.example.fitflow.data.model

import java.time.LocalDate

enum class StepSource {
    SENSOR,
    MANUAL
}

data class DailyHealthMetrics(
    val date: LocalDate,
    val steps: Int,
    val waterIntakeMl: Int,
    val waterGoalMl: Int,
    val stepSource: StepSource
)
