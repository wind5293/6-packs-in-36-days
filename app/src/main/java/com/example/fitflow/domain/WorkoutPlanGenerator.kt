package com.example.fitflow.domain

import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.WorkoutExercise
import com.example.fitflow.data.model.FitnessGoal

object WorkoutPlangenerator {

    private val weightLossPool = listOf(
        WorkoutExercise("Cardio", "Jumping Jack",   sets = 3, reps = 40, kcal = 50, durationSec = 60),
        WorkoutExercise("Cardio", "Burpee",         sets = 3, reps = 12, kcal = 80, durationSec = 60),
        WorkoutExercise("Cardio", "Mountain Climber", sets = 3, reps = 30, kcal = 60, durationSec = 60),
        WorkoutExercise("Cardio", "High Knee",      sets = 3, reps = 30, kcal = 60, durationSec = 60),
        WorkoutExercise("Cardio", "Jump Squat",     sets = 3, reps = 15, kcal = 70, durationSec = 60),
        WorkoutExercise("Cardio", "Sprint in Place",sets = 3, reps = 30, kcal = 55, durationSec = 60),
        WorkoutExercise("Cardio", "Jump Rope",      sets = 3, reps = 50, kcal = 65, durationSec = 60),
        WorkoutExercise("Cardio", "Skater Jump",    sets = 3, reps = 20, kcal = 55, durationSec = 60),
        WorkoutExercise("Cardio", "Star Jump",      sets = 3, reps = 20, kcal = 45, durationSec = 60),
    )

    private val muscleGainPool = listOf(
        WorkoutExercise("Strength", "Push-up",        sets = 3, reps = 12, kcal = 55, durationSec = 60),
        WorkoutExercise("Strength", "Squat",          sets = 3, reps = 15, kcal = 50, durationSec = 60),
        WorkoutExercise("Strength", "Lunge",          sets = 3, reps = 12, kcal = 45, durationSec = 60),
        WorkoutExercise("Strength", "Plank",          sets = 3, reps = 60, kcal = 30, durationSec = 60),
        WorkoutExercise("Strength", "Pike Push-up",   sets = 3, reps = 10, kcal = 40, durationSec = 60),
        WorkoutExercise("Strength", "Tricep Dip",     sets = 3, reps = 12, kcal = 45, durationSec = 60),
        WorkoutExercise("Strength", "Glute Bridge",   sets = 3, reps = 15, kcal = 35, durationSec = 60),
        WorkoutExercise("Strength", "Diamond Push-up",sets = 3, reps = 10, kcal = 50, durationSec = 60),
        WorkoutExercise("Strength", "Wall Sit",       sets = 3, reps = 45, kcal = 25, durationSec = 60),
    )

    private val endurancePool = listOf(
        WorkoutExercise("Endurance", "Burpee",         sets = 3, reps = 10, kcal = 70, durationSec = 60),
        WorkoutExercise("Endurance", "Push-up",        sets = 3, reps = 12, kcal = 55, durationSec = 60),
        WorkoutExercise("Endurance", "Mountain Climber", sets = 3, reps = 30, kcal = 60, durationSec = 60),
        WorkoutExercise("Endurance", "Jump Squat",     sets = 3, reps = 15, kcal = 65, durationSec = 60),
        WorkoutExercise("Endurance", "Plank",          sets = 3, reps = 60, kcal = 30, durationSec = 60),
        WorkoutExercise("Endurance", "Jumping Jack",   sets = 3, reps = 40, kcal = 50, durationSec = 60),
        WorkoutExercise("Endurance", "High Knee",      sets = 3, reps = 30, kcal = 55, durationSec = 60),
        WorkoutExercise("Endurance", "Lunge",          sets = 3, reps = 12, kcal = 45, durationSec = 60),
        WorkoutExercise("Endurance", "Tricep Dip",     sets = 3, reps = 12, kcal = 40, durationSec = 60),
    )

    private val maintenancePool = listOf(
        WorkoutExercise("Maintenance", "Push-up",        sets = 3, reps = 10, kcal = 50, durationSec = 60),
        WorkoutExercise("Maintenance", "Squat",          sets = 3, reps = 12, kcal = 45, durationSec = 60),
        WorkoutExercise("Maintenance", "Jumping Jack",   sets = 3, reps = 30, kcal = 40, durationSec = 60),
        WorkoutExercise("Maintenance", "Plank",          sets = 3, reps = 45, kcal = 25, durationSec = 60),
        WorkoutExercise("Maintenance", "Lunge",          sets = 3, reps = 10, kcal = 40, durationSec = 60),
        WorkoutExercise("Maintenance", "Mountain Climber", sets = 3, reps = 20, kcal = 50, durationSec = 60),
        WorkoutExercise("Maintenance", "Glute Bridge",   sets = 3, reps = 15, kcal = 35, durationSec = 60),
        WorkoutExercise("Maintenance", "High Knee",      sets = 3, reps = 25, kcal = 45, durationSec = 60),
        WorkoutExercise("Maintenance", "Dip",            sets = 3, reps = 10, kcal = 40, durationSec = 60),
    )

    private val restDaysLight = setOf(6, 7, 13, 14, 20, 21, 27, 28)
    private val restDaysHeavy = setOf(3, 6, 7, 10, 13, 14, 17, 20, 21, 24, 27, 28)

    private fun pickWorkoutExercises(pool: List<WorkoutExercise>, day: Int, weekIndex: Int): List<WorkoutExercise> {
        val index = (day - 1) % pool.size
        val selected = mutableListOf<WorkoutExercise>()
        for (i in 0 until 3) {
            val WorkoutExercise = pool[(index + i) % pool.size]
            selected.add(
                if (weekIndex >= 2) WorkoutExercise.copy(sets = WorkoutExercise.sets + 1)
                else WorkoutExercise
            )
        }
        return selected
    }

    fun generatePlan(goal: FitnessGoal): List<DayPlan> {
        val (pool, restDays) = when (goal) {
            FitnessGoal.WEIGHT_LOSS -> Pair(weightLossPool, restDaysLight)
            FitnessGoal.MUSCLE_GAIN -> Pair(muscleGainPool, restDaysHeavy)
            FitnessGoal.ENDURANCE   -> Pair(endurancePool, restDaysLight)
            FitnessGoal.MAINTENANCE -> Pair(maintenancePool, restDaysHeavy)
        }
        return (1..30).map { day ->
            if (day in restDays) {
                DayPlan(dayNumber = day, isRest = true, workoutExercises = emptyList())
            } else {
                val weekIndex = (day - 1) / 7
                DayPlan(
                    dayNumber = day,
                    isRest = false,
                    workoutExercises = pickWorkoutExercises(pool, day, weekIndex),
                )
            }
        }
    }
}
