package com.example.fitflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.fitflow.data.model.Exercise

@Entity(tableName = "exercises")
@TypeConverters(Converters::class)
data class ExerciseEntity(
    @PrimaryKey val name: String,
    val difficulty: String,
    val exercise_type: String,
    val log_type: String,
    val target_muscles: List<String>,
    val equipment: List<String>,
    val instructions: List<String>,
    val local_gifs: List<String>,
) {
    // Convert sang model để UI dùng
    fun toExercise() = Exercise(
        name = name,
        difficulty = difficulty,
        exercise_type = exercise_type,
        log_type = log_type,
        target_muscles = target_muscles,
        equipment = equipment,
        instructions = instructions,
        local_gifs = local_gifs,
    )

    companion object {
        // Convert từ model để insert vào DB
        fun fromExercise(e: Exercise) = ExerciseEntity(
            name = e.name,
            difficulty = e.difficulty,
            exercise_type = e.exercise_type,
            log_type = e.log_type,
            target_muscles = e.target_muscles,
            equipment = e.equipment,
            instructions = e.instructions,
            local_gifs = e.local_gifs,
        )
    }
}