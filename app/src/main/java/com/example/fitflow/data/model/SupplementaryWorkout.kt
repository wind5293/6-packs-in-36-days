package com.example.fitflow.data.model

import androidx.compose.ui.graphics.Color

/**
 * Standalone bonus workout (Push Your Limits) — not part of the 30-day plan calendar.
 */
data class SupplementaryWorkout(
    val id: String,
    val title: String,
    val subtitle: String,
    val difficulty: String,
    val durationMinutes: Int,
    val muscleGroup: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val imageRes: Int? = null,
    val exercises: List<WorkoutExercise>
) {
    fun toDayPlan(): DayPlan = DayPlan(
        dayNumber = 0,
        isRest = false,
        workoutExercises = exercises,
        title = title,
        difficulty = difficulty,
        muscleGroup = muscleGroup
    )

    fun withEnrichedExercises(enriched: List<WorkoutExercise>): SupplementaryWorkout =
        copy(exercises = enriched)
}
