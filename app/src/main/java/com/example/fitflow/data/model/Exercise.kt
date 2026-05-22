package com.example.fitflow.data.model

import androidx.room.Entity

@Entity(tableName = "exercises")
data class Exercise(
    val name: String,
    val difficulty: String,
    val exercise_type: String,
    val log_type: String,
    val target_muscles: List<String>,
    val equipment: List<String>,
    val instructions: List<String>,
    val local_gifs: List<String>,
) {
}
