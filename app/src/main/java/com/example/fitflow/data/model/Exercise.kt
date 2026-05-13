package com.example.fitflow.data.model

data class Exercise(
    val category: String,
    val name: String,
    val sets: Int,
    val reps: Int,
    val kcal: Int,
    val durationSec: Int,
    val difficulty: String = calculateDifficulty(kcal),  // EASY, MEDIUM, HARD
    val muscleGroups: List<String> = emptyList(),        // e.g., ["Chest", "Arms", "Core"]
) {
    companion object {
        fun calculateDifficulty(kcal: Int): String {
            return when {
                kcal <= 35 -> "EASY"
                kcal in 36..65 -> "MEDIUM"
                else -> "HARD"
            }
        }
    }
}