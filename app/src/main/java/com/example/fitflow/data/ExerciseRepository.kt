package com.example.fitflow.data

import com.example.fitflow.data.model.Exercise

object ExerciseRepository {

    // Helper function to map muscle groups based on exercise name
    private fun getMuscleGroups(name: String): List<String> {
        return when {
            name.contains("Push", ignoreCase = true) -> listOf("Chest", "Arms", "Shoulders")
            name.contains("Squat", ignoreCase = true) -> listOf("Legs", "Glutes", "Quads")
            name.contains("Lunge", ignoreCase = true) -> listOf("Legs", "Glutes", "Quads")
            name.contains("Plank", ignoreCase = true) -> listOf("Core", "Shoulders")
            name.contains("Dip", ignoreCase = true) -> listOf("Arms", "Chest", "Shoulders")
            name.contains("Burpee", ignoreCase = true) -> listOf("Full Body", "Chest", "Legs", "Core")
            name.contains("Jump", ignoreCase = true) -> listOf("Legs", "Full Body", "Cardio")
            name.contains("Climbing", ignoreCase = true) -> listOf("Core", "Arms", "Full Body")
            name.contains("Sprint", ignoreCase = true) -> listOf("Legs", "Cardio", "Full Body")
            name.contains("Rope", ignoreCase = true) -> listOf("Arms", "Full Body", "Cardio")
            name.contains("Skater", ignoreCase = true) -> listOf("Legs", "Cardio", "Core")
            name.contains("Star", ignoreCase = true) -> listOf("Full Body", "Cardio", "Legs")
            name.contains("Bridge", ignoreCase = true) -> listOf("Glutes", "Core", "Legs")
            name.contains("Sit", ignoreCase = true) -> listOf("Legs", "Quads")
            name.contains("Sprint", ignoreCase = true) -> listOf("Legs", "Cardio")
            name.contains("Knee", ignoreCase = true) -> listOf("Legs", "Cardio", "Core")
            else -> listOf("Full Body")
        }
    }

    // 4 pools từ WorkoutPlanGenerator — tổng 36 exercises
    private val allExercises = listOf(
        // CARDIO — Weight Loss Pool (9 exercises)
        Exercise("Cardio", "Jumping Jack",   sets = 3, reps = 40, kcal = 50, durationSec = 60, muscleGroups = getMuscleGroups("Jumping Jack")),
        Exercise("Cardio", "Burpee",         sets = 3, reps = 12, kcal = 80, durationSec = 60, muscleGroups = getMuscleGroups("Burpee")),
        Exercise("Cardio", "Mountain Climber", sets = 3, reps = 30, kcal = 60, durationSec = 60, muscleGroups = getMuscleGroups("Mountain Climber")),
        Exercise("Cardio", "High Knee",      sets = 3, reps = 30, kcal = 60, durationSec = 60, muscleGroups = getMuscleGroups("High Knee")),
        Exercise("Cardio", "Jump Squat",     sets = 3, reps = 15, kcal = 70, durationSec = 60, muscleGroups = getMuscleGroups("Jump Squat")),
        Exercise("Cardio", "Sprint in Place",sets = 3, reps = 30, kcal = 55, durationSec = 60, muscleGroups = getMuscleGroups("Sprint")),
        Exercise("Cardio", "Jump Rope",      sets = 3, reps = 50, kcal = 65, durationSec = 60, muscleGroups = getMuscleGroups("Jump Rope")),
        Exercise("Cardio", "Skater Jump",    sets = 3, reps = 20, kcal = 55, durationSec = 60, muscleGroups = getMuscleGroups("Skater Jump")),
        Exercise("Cardio", "Star Jump",      sets = 3, reps = 20, kcal = 45, durationSec = 60, muscleGroups = getMuscleGroups("Star Jump")),

        // STRENGTH — Muscle Gain Pool (9 exercises)
        Exercise("Strength", "Push-up",        sets = 3, reps = 12, kcal = 55, durationSec = 60, muscleGroups = getMuscleGroups("Push-up")),
        Exercise("Strength", "Squat",          sets = 3, reps = 15, kcal = 50, durationSec = 60, muscleGroups = getMuscleGroups("Squat")),
        Exercise("Strength", "Lunge",          sets = 3, reps = 12, kcal = 45, durationSec = 60, muscleGroups = getMuscleGroups("Lunge")),
        Exercise("Strength", "Plank",          sets = 3, reps = 60, kcal = 30, durationSec = 60, muscleGroups = getMuscleGroups("Plank")),
        Exercise("Strength", "Pike Push-up",   sets = 3, reps = 10, kcal = 40, durationSec = 60, muscleGroups = getMuscleGroups("Pike Push-up")),
        Exercise("Strength", "Tricep Dip",     sets = 3, reps = 12, kcal = 45, durationSec = 60, muscleGroups = getMuscleGroups("Tricep Dip")),
        Exercise("Strength", "Glute Bridge",   sets = 3, reps = 15, kcal = 35, durationSec = 60, muscleGroups = getMuscleGroups("Glute Bridge")),
        Exercise("Strength", "Diamond Push-up",sets = 3, reps = 10, kcal = 50, durationSec = 60, muscleGroups = getMuscleGroups("Push-up")),
        Exercise("Strength", "Wall Sit",       sets = 3, reps = 45, kcal = 25, durationSec = 60, muscleGroups = getMuscleGroups("Sit")),

        // ENDURANCE — Endurance Pool (9 exercises)
        Exercise("Endurance", "Burpee",         sets = 3, reps = 10, kcal = 70, durationSec = 60, muscleGroups = getMuscleGroups("Burpee")),
        Exercise("Endurance", "Push-up",        sets = 3, reps = 12, kcal = 55, durationSec = 60, muscleGroups = getMuscleGroups("Push-up")),
        Exercise("Endurance", "Mountain Climber", sets = 3, reps = 30, kcal = 60, durationSec = 60, muscleGroups = getMuscleGroups("Mountain Climber")),
        Exercise("Endurance", "Jump Squat",     sets = 3, reps = 15, kcal = 65, durationSec = 60, muscleGroups = getMuscleGroups("Jump Squat")),
        Exercise("Endurance", "Plank",          sets = 3, reps = 60, kcal = 30, durationSec = 60, muscleGroups = getMuscleGroups("Plank")),
        Exercise("Endurance", "Jumping Jack",   sets = 3, reps = 40, kcal = 50, durationSec = 60, muscleGroups = getMuscleGroups("Jumping Jack")),
        Exercise("Endurance", "High Knee",      sets = 3, reps = 30, kcal = 55, durationSec = 60, muscleGroups = getMuscleGroups("High Knee")),
        Exercise("Endurance", "Lunge",          sets = 3, reps = 12, kcal = 45, durationSec = 60, muscleGroups = getMuscleGroups("Lunge")),
        Exercise("Endurance", "Tricep Dip",     sets = 3, reps = 12, kcal = 40, durationSec = 60, muscleGroups = getMuscleGroups("Tricep Dip")),

        // MAINTENANCE — Maintenance Pool (9 exercises)
        Exercise("Maintenance", "Push-up",        sets = 3, reps = 10, kcal = 50, durationSec = 60, muscleGroups = getMuscleGroups("Push-up")),
        Exercise("Maintenance", "Squat",          sets = 3, reps = 12, kcal = 45, durationSec = 60, muscleGroups = getMuscleGroups("Squat")),
        Exercise("Maintenance", "Jumping Jack",   sets = 3, reps = 30, kcal = 40, durationSec = 60, muscleGroups = getMuscleGroups("Jumping Jack")),
        Exercise("Maintenance", "Plank",          sets = 3, reps = 45, kcal = 25, durationSec = 60, muscleGroups = getMuscleGroups("Plank")),
        Exercise("Maintenance", "Lunge",          sets = 3, reps = 10, kcal = 40, durationSec = 60, muscleGroups = getMuscleGroups("Lunge")),
        Exercise("Maintenance", "Mountain Climber", sets = 3, reps = 20, kcal = 50, durationSec = 60, muscleGroups = getMuscleGroups("Mountain Climber")),
        Exercise("Maintenance", "Glute Bridge",   sets = 3, reps = 15, kcal = 35, durationSec = 60, muscleGroups = getMuscleGroups("Glute Bridge")),
        Exercise("Maintenance", "High Knee",      sets = 3, reps = 25, kcal = 45, durationSec = 60, muscleGroups = getMuscleGroups("High Knee")),
        Exercise("Maintenance", "Dip",            sets = 3, reps = 10, kcal = 40, durationSec = 60, muscleGroups = getMuscleGroups("Dip")),
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
     * Lấy danh sách các muscle groups duy nhất
     */
    fun getMuscleGroupsList(): List<String> {
        return allExercises.flatMap { it.muscleGroups }.distinct().sorted()
    }

    /**
     * Lấy danh sách difficulties
     */
    fun getDifficultiesList(): List<String> = listOf("EASY", "MEDIUM", "HARD")

    /**
     * Filter exercises theo tất cả criteria
     */
    fun filterExercises(
        category: String = "ALL",
        searchQuery: String = "",
        minCalories: Int = 25,
        maxCalories: Int = 80,
        difficulty: String = "ALL",
        muscleGroup: String = "ALL"
    ): List<Exercise> {
        return allExercises.filter { exercise ->
            // Category filter
            val categoryMatch = category == "ALL" || exercise.category == category

            // Search filter (by name)
            val searchMatch = searchQuery.isEmpty() || exercise.name.contains(searchQuery, ignoreCase = true)

            // Calories filter
            val caloriesMatch = exercise.kcal in minCalories..maxCalories

            // Difficulty filter
            val difficultyMatch = difficulty == "ALL" || exercise.difficulty == difficulty

            // Muscle group filter
            val muscleGroupMatch = muscleGroup == "ALL" || exercise.muscleGroups.contains(muscleGroup)

            // All conditions must be true
            categoryMatch && searchMatch && caloriesMatch && difficultyMatch && muscleGroupMatch
        }
    }

    /**
     * Filter exercises theo category
     */
    fun getExercisesByCategory(category: String): List<Exercise> {
        return filterExercises(category = category)
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
