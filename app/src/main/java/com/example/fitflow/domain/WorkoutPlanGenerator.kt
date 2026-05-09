package com.example.fitflow.domain

import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.Exercise
import com.example.fitflow.data.model.FitnessGoal

object WorkoutPlangenerator {

    private val weightLossPool = listOf(
        Exercise("Jumping Jack",   sets = 3, reps = 40, kcal = 50, durationSec = 60),
        Exercise("Burpee",         sets = 3, reps = 12, kcal = 80, durationSec = 60),
        Exercise("Mountain Climber", sets = 3, reps = 30, kcal = 60, durationSec = 60),
        Exercise("High Knee",      sets = 3, reps = 30, kcal = 60, durationSec = 60),
        Exercise("Jump Squat",     sets = 3, reps = 15, kcal = 70, durationSec = 60),
        Exercise("Sprint in Place",sets = 3, reps = 30, kcal = 55, durationSec = 60),
        Exercise("Jump Rope",      sets = 3, reps = 50, kcal = 65, durationSec = 60),
        Exercise("Skater Jump",    sets = 3, reps = 20, kcal = 55, durationSec = 60),
        Exercise("Star Jump",      sets = 3, reps = 20, kcal = 45, durationSec = 60),
    )

    private val muscleGainPool = listOf(
        Exercise("Push-up",        sets = 3, reps = 12, kcal = 55, durationSec = 60),
        Exercise("Squat",          sets = 3, reps = 15, kcal = 50, durationSec = 60),
        Exercise("Lunge",          sets = 3, reps = 12, kcal = 45, durationSec = 60),
        Exercise("Plank",          sets = 3, reps = 60, kcal = 30, durationSec = 60),
        Exercise("Pike Push-up",   sets = 3, reps = 10, kcal = 40, durationSec = 60),
        Exercise("Tricep Dip",     sets = 3, reps = 12, kcal = 45, durationSec = 60),
        Exercise("Glute Bridge",   sets = 3, reps = 15, kcal = 35, durationSec = 60),
        Exercise("Diamond Push-up",sets = 3, reps = 10, kcal = 50, durationSec = 60),
        Exercise("Wall Sit",       sets = 3, reps = 45, kcal = 25, durationSec = 60),
    )

    private val endurancePool = listOf(
        Exercise("Burpee",         sets = 3, reps = 10, kcal = 70, durationSec = 60),
        Exercise("Push-up",        sets = 3, reps = 12, kcal = 55, durationSec = 60),
        Exercise("Mountain Climber", sets = 3, reps = 30, kcal = 60, durationSec = 60),
        Exercise("Jump Squat",     sets = 3, reps = 15, kcal = 65, durationSec = 60),
        Exercise("Plank",          sets = 3, reps = 60, kcal = 30, durationSec = 60),
        Exercise("Jumping Jack",   sets = 3, reps = 40, kcal = 50, durationSec = 60),
        Exercise("High Knee",      sets = 3, reps = 30, kcal = 55, durationSec = 60),
        Exercise("Lunge",          sets = 3, reps = 12, kcal = 45, durationSec = 60),
        Exercise("Tricep Dip",     sets = 3, reps = 12, kcal = 40, durationSec = 60),
    )

    private val maintenancePool = listOf(
        Exercise("Push-up",        sets = 3, reps = 10, kcal = 50, durationSec = 60),
        Exercise("Squat",          sets = 3, reps = 12, kcal = 45, durationSec = 60),
        Exercise("Jumping Jack",   sets = 3, reps = 30, kcal = 40, durationSec = 60),
        Exercise("Plank",          sets = 3, reps = 45, kcal = 25, durationSec = 60),
        Exercise("Lunge",          sets = 3, reps = 10, kcal = 40, durationSec = 60),
        Exercise("Mountain Climber", sets = 3, reps = 20, kcal = 50, durationSec = 60),
        Exercise("Glute Bridge",   sets = 3, reps = 15, kcal = 35, durationSec = 60),
        Exercise("High Knee",      sets = 3, reps = 25, kcal = 45, durationSec = 60),
        Exercise("Dip",            sets = 3, reps = 10, kcal = 40, durationSec = 60),
    )

    private val restDaysLight = setOf(6, 7, 13, 14, 20, 21, 27, 28)
    private val restDaysHeavy = setOf(3, 6, 7, 10, 13, 14, 17, 20, 21, 24, 27, 28)

    private fun pickExercises(pool: List<Exercise>, day: Int, weekIndex: Int): List<Exercise> {
        val index = (day - 1) % pool.size
        val selected = mutableListOf<Exercise>()
        for (i in 0 until 3) {
            val exercise = pool[(index + i) % pool.size]
            selected.add(
                if (weekIndex >= 2) exercise.copy(sets = exercise.sets + 1)
                else exercise
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
                DayPlan(dayNumber = day, isRest = true, exercises = emptyList())
            } else {
                val weekIndex = (day - 1) / 7
                DayPlan(dayNumber = day, isRest = false, exercises = pickExercises(pool, day, weekIndex))
            }
        }
    }
}
