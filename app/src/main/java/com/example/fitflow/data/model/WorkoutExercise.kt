package com.example.fitflow.data.model

data class WorkoutExercise(
    val category: String,
    val name: String,
    val sets: Int,
    val reps: Int,
    val kcal: Int,
    val durationSec: Int
)