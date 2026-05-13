package com.example.fitflow.data

import com.example.fitflow.data.model.Exercise

object ExerciseRepository {

    // 4 pools từ WorkoutPlanGenerator — tổng 36 exercises
    private val allExercises = listOf(
        // CARDIO — Weight Loss Pool (9 exercises)
        Exercise("Cardio", "Jumping Jack",   sets = 3, reps = 40, kcal = 50, durationSec = 60),
        Exercise("Cardio", "Burpee",         sets = 3, reps = 12, kcal = 80, durationSec = 60),
        Exercise("Cardio", "Mountain Climber", sets = 3, reps = 30, kcal = 60, durationSec = 60),
        Exercise("Cardio", "High Knee",      sets = 3, reps = 30, kcal = 60, durationSec = 60),
        Exercise("Cardio", "Jump Squat",     sets = 3, reps = 15, kcal = 70, durationSec = 60),
        Exercise("Cardio", "Sprint in Place",sets = 3, reps = 30, kcal = 55, durationSec = 60),
        Exercise("Cardio", "Jump Rope",      sets = 3, reps = 50, kcal = 65, durationSec = 60),
        Exercise("Cardio", "Skater Jump",    sets = 3, reps = 20, kcal = 55, durationSec = 60),
        Exercise("Cardio", "Star Jump",      sets = 3, reps = 20, kcal = 45, durationSec = 60),

        // STRENGTH — Muscle Gain Pool (9 exercises)
        Exercise("Strength", "Push-up",        sets = 3, reps = 12, kcal = 55, durationSec = 60),
        Exercise("Strength", "Squat",          sets = 3, reps = 15, kcal = 50, durationSec = 60),
        Exercise("Strength", "Lunge",          sets = 3, reps = 12, kcal = 45, durationSec = 60),
        Exercise("Strength", "Plank",          sets = 3, reps = 60, kcal = 30, durationSec = 60),
        Exercise("Strength", "Pike Push-up",   sets = 3, reps = 10, kcal = 40, durationSec = 60),
        Exercise("Strength", "Tricep Dip",     sets = 3, reps = 12, kcal = 45, durationSec = 60),
        Exercise("Strength", "Glute Bridge",   sets = 3, reps = 15, kcal = 35, durationSec = 60),
        Exercise("Strength", "Diamond Push-up",sets = 3, reps = 10, kcal = 50, durationSec = 60),
        Exercise("Strength", "Wall Sit",       sets = 3, reps = 45, kcal = 25, durationSec = 60),

        // ENDURANCE — Endurance Pool (9 exercises)
        Exercise("Endurance", "Burpee",         sets = 3, reps = 10, kcal = 70, durationSec = 60),
        Exercise("Endurance", "Push-up",        sets = 3, reps = 12, kcal = 55, durationSec = 60),
        Exercise("Endurance", "Mountain Climber", sets = 3, reps = 30, kcal = 60, durationSec = 60),
        Exercise("Endurance", "Jump Squat",     sets = 3, reps = 15, kcal = 65, durationSec = 60),
        Exercise("Endurance", "Plank",          sets = 3, reps = 60, kcal = 30, durationSec = 60),
        Exercise("Endurance", "Jumping Jack",   sets = 3, reps = 40, kcal = 50, durationSec = 60),
        Exercise("Endurance", "High Knee",      sets = 3, reps = 30, kcal = 55, durationSec = 60),
        Exercise("Endurance", "Lunge",          sets = 3, reps = 12, kcal = 45, durationSec = 60),
        Exercise("Endurance", "Tricep Dip",     sets = 3, reps = 12, kcal = 40, durationSec = 60),

        // MAINTENANCE — Maintenance Pool (9 exercises)
        Exercise("Maintenance", "Push-up",        sets = 3, reps = 10, kcal = 50, durationSec = 60),
        Exercise("Maintenance", "Squat",          sets = 3, reps = 12, kcal = 45, durationSec = 60),
        Exercise("Maintenance", "Jumping Jack",   sets = 3, reps = 30, kcal = 40, durationSec = 60),
        Exercise("Maintenance", "Plank",          sets = 3, reps = 45, kcal = 25, durationSec = 60),
        Exercise("Maintenance", "Lunge",          sets = 3, reps = 10, kcal = 40, durationSec = 60),
        Exercise("Maintenance", "Mountain Climber", sets = 3, reps = 20, kcal = 50, durationSec = 60),
        Exercise("Maintenance", "Glute Bridge",   sets = 3, reps = 15, kcal = 35, durationSec = 60),
        Exercise("Maintenance", "High Knee",      sets = 3, reps = 25, kcal = 45, durationSec = 60),
        Exercise("Maintenance", "Dip",            sets = 3, reps = 10, kcal = 40, durationSec = 60),
    )

    /**
     * Lấy tất cả exercises
     */
    fun getAllExercises(): List<Exercise> = allExercises

    /**
     * Lấy danh sách các category duy nhất
     */
    fun getCategoryList(): List<String> = allExercises.map { it.category }.distinct().sorted()

    /**
     * Filter exercises theo category
     */
    fun getExercisesByCategory(category: String): List<Exercise> {
        return if (category == "ALL") {
            allExercises
        } else {
            allExercises.filter { it.category == category }
        }
    }

    /**
     * Tìm exercise theo tên
     */
    fun getExerciseByName(name: String): Exercise? {
        return allExercises.find { it.name == name }
    }

    /**
     * Lấy exercises theo category để hiển thị trong Library (sorted by name)
     */
    fun getExercisesCategorized(): Map<String, List<Exercise>> {
        return allExercises.groupBy { it.category }.mapValues { (_, exercises) ->
            exercises.sortedBy { it.name }
        }
    }
}
