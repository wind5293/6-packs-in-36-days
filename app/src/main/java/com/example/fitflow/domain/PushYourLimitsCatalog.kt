package com.example.fitflow.domain

import androidx.compose.ui.graphics.Color
import com.example.fitflow.data.ExerciseRepository
import com.example.fitflow.data.model.SupplementaryWorkout
import com.example.fitflow.data.model.WorkoutExercise

/**
 * Five region-focused bonus workouts — aligned with common training splits.
 */
object PushYourLimitsCatalog {

    private fun ex(
        category: String,
        name: String,
        sets: Int = 3,
        reps: Int = 0,
        kcal: Int = 0,
        durationSec: Int = 45,
        description: String = ""
    ) = WorkoutExercise(
        category = category,
        name = name,
        sets = sets,
        reps = reps,
        kcal = kcal,
        durationSec = durationSec,
        description = description
    )

    private val allWorkouts: List<SupplementaryWorkout> = listOf(
        SupplementaryWorkout(
            id = "core_abs",
            title = "Defined Abs HIIT",
            subtitle = "Core & waistline",
            difficulty = "Intermediate",
            durationMinutes = 19,
            muscleGroup = "Core",
            gradientStart = Color(0xFF6B4EFF),
            gradientEnd = Color(0xFF00B4D8),
            imageRes = com.example.fitflow.R.drawable.abs,
            exercises = listOf(
                ex("Core", "Crunch", sets = 3, reps = 20, kcal = 35, durationSec = 45,
                    description = "Lift shoulder blades off the floor. Exhale on the way up."),
                ex("Core", "Plank", sets = 3, reps = 0, kcal = 30, durationSec = 45,
                    description = "Neutral spine, squeeze glutes. Hold without sagging hips."),
                ex("Core", "Mountain Climber", sets = 3, reps = 24, kcal = 55, durationSec = 40,
                    description = "Drive knees toward chest alternately from high plank."),
                ex("Core", "Leg Raise", sets = 3, reps = 15, kcal = 40, durationSec = 45,
                    description = "Lower legs with control. Press lower back into the floor."),
                ex("Core", "Sit-Up", sets = 2, reps = 18, kcal = 35, durationSec = 40,
                    description = "Full sit-up with controlled descent.")
            )
        ),
        SupplementaryWorkout(
            id = "upper_push",
            title = "Chest & Shoulders Blast",
            subtitle = "Push muscles",
            difficulty = "Intermediate",
            durationMinutes = 18,
            muscleGroup = "Upper Push",
            gradientStart = Color(0xFFFF5F07),
            gradientEnd = Color(0xFFFFB347),
            imageRes = com.example.fitflow.R.drawable.chest,
            exercises = listOf(
                ex("Strength", "Push-Up", sets = 4, reps = 12, kcal = 55, durationSec = 0,
                    description = "Chest to floor, full lockout. Scale on knees if needed."),
                ex("Strength", "Pike Push-up", sets = 3, reps = 10, kcal = 45, durationSec = 0,
                    description = "Hips high, head travels between hands for shoulder focus."),
                ex("Strength", "Diamond Push-up", sets = 3, reps = 10, kcal = 50, durationSec = 0,
                    description = "Hands form a diamond under chest. Elbows stay close."),
                ex("Strength", "Tricep Dip", sets = 3, reps = 12, kcal = 40, durationSec = 0,
                    description = "Use a chair or bench. Shoulders down, elbows back."),
                ex("Strength", "Plank", sets = 2, reps = 0, kcal = 25, durationSec = 45,
                    description = "Anti-extension finisher for trunk stability.")
            )
        ),
        SupplementaryWorkout(
            id = "lower_body",
            title = "Legs & Glutes Burn",
            subtitle = "Lower body power",
            difficulty = "Beginner",
            durationMinutes = 16,
            muscleGroup = "Lower Body",
            gradientStart = Color(0xFF00A86B),
            gradientEnd = Color(0xFF7AE582),
            imageRes = com.example.fitflow.R.drawable.glutes,
            exercises = listOf(
                ex("Strength", "Squat", sets = 4, reps = 15, kcal = 50, durationSec = 0,
                    description = "Sit hips back, knees track over toes, chest up."),
                ex("Strength", "Lunge", sets = 3, reps = 12, kcal = 45, durationSec = 0,
                    description = "Step long, back knee hovers. Alternate legs each set."),
                ex("Strength", "Glute Bridge", sets = 3, reps = 15, kcal = 35, durationSec = 0,
                    description = "Drive hips up, pause at top, squeeze glutes."),
                ex("Cardio", "Jump Squat", sets = 3, reps = 12, kcal = 65, durationSec = 0,
                    description = "Land softly with bent knees after each jump."),
                ex("Strength", "Wall Sit", sets = 2, reps = 0, kcal = 25, durationSec = 45,
                    description = "Thighs parallel to floor. Breathe steadily through the hold.")
            )
        ),
        SupplementaryWorkout(
            id = "back_posture",
            title = "Back & Posture Strength",
            subtitle = "Posterior chain",
            difficulty = "Beginner",
            durationMinutes = 15,
            muscleGroup = "Back",
            gradientStart = Color(0xFF5C6BC0),
            gradientEnd = Color(0xFF9FA8DA),
            imageRes = com.example.fitflow.R.drawable.back,
            exercises = listOf(
                ex("Strength", "Superman", sets = 3, reps = 15, kcal = 35, durationSec = 40,
                    description = "Lift chest and legs slightly off the floor. Hold briefly at top."),
                ex("Strength", "Bird Dog", sets = 3, reps = 12, kcal = 30, durationSec = 45,
                    description = "Opposite arm and leg extend. Keep hips level."),
                ex("Core", "Plank", sets = 3, reps = 0, kcal = 30, durationSec = 45,
                    description = "Brace core — foundation for healthy posture."),
                ex("Strength", "Glute Bridge", sets = 3, reps = 15, kcal = 35, durationSec = 0,
                    description = "Supports lumbar stability and hip extension."),
                ex("Core", "Mountain Climber", sets = 2, reps = 20, kcal = 40, durationSec = 35,
                    description = "Light cardio finisher while keeping a strong plank.")
            )
        ),
        SupplementaryWorkout(
            id = "full_body_hiit",
            title = "Total Body Ignite",
            subtitle = "Full-body conditioning",
            difficulty = "Advanced",
            durationMinutes = 20,
            muscleGroup = "Full Body",
            gradientStart = Color(0xFFE91E8C),
            gradientEnd = Color(0xFFFF5722),
            imageRes = com.example.fitflow.R.drawable.body,
            exercises = listOf(
                ex("Cardio", "Burpee", sets = 4, reps = 10, kcal = 80, durationSec = 0,
                    description = "Chest to floor, jump at top. Pace for sustainability."),
                ex("Cardio", "Jumping Jack", sets = 3, reps = 40, kcal = 50, durationSec = 0,
                    description = "Arms overhead, land with soft knees."),
                ex("Cardio", "Mountain Climber", sets = 3, reps = 30, kcal = 60, durationSec = 40,
                    description = "Fast feet, stable shoulders over wrists."),
                ex("Cardio", "High Knee", sets = 3, reps = 30, kcal = 55, durationSec = 40,
                    description = "Drive knees to hip height. Pump arms for rhythm."),
                ex("Cardio", "Jump Squat", sets = 3, reps = 15, kcal = 70, durationSec = 0,
                    description = "Explosive squat jump. Reset stance between reps if needed.")
            )
        )
    )

    fun all(): List<SupplementaryWorkout> = allWorkouts

    fun findById(id: String): SupplementaryWorkout? = allWorkouts.find { it.id == id }

    suspend fun enrichExercises(
        exercises: List<WorkoutExercise>,
        repository: ExerciseRepository
    ): List<WorkoutExercise> = exercises.map { exercise ->
        val gif = repository.getGifFileName(exercise.name)
            ?: repository.findBestMatchByName(exercise.name)?.local_gifs?.firstOrNull()
            ?: ""
        exercise.copy(gifFileName = gif)
    }

    suspend fun findEnrichedById(
        id: String,
        repository: ExerciseRepository
    ): SupplementaryWorkout? {
        val workout = findById(id) ?: return null
        val enriched = enrichExercises(workout.exercises, repository)
        return workout.withEnrichedExercises(enriched)
    }
}
