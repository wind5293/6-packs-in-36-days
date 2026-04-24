package com.example.fitflow.domain

import com.example.fitflow.data.model.BmiCategory
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.Exercise

object WorkoutPlangenerator {
    private val underweightPool = listOf(
        Exercise("Push-up", 3, 10, 30),
        Exercise("Squat", 3, 10, 30),
        Exercise("Lunges", 3, 10, 30),
        Exercise("Lunge", 3, 10, 30),
        Exercise("Plank", 3, 10, 30),
        Exercise("Pike Push-up", 3, 10, 30),
        Exercise("Glute Bridge", 3, 10, 30),
        Exercise("Dip", 3, 10, 30),
        Exercise("Wall sit", 3, 10, 30),
    )

    private val normalPool = listOf(
        Exercise("Burpee", sets = 3, reps = 12, kcal = 80),
        Exercise("Squat Jump", sets = 3, reps = 15, kcal = 70),
        Exercise("Mountain Climber", sets = 3, reps = 30, kcal = 60),
        Exercise("Push-up", sets = 3, reps = 12, kcal = 55),
        Exercise("Plank", sets = 3, reps = 60, kcal = 30),
        Exercise("Jumping Jack", sets = 3, reps = 40, kcal = 50),
        Exercise("High Knee", sets = 3, reps = 30, kcal = 60),
        Exercise("Tricep Dip", sets = 3, reps = 12, kcal = 45)
    )

    private val overweightPool = listOf(
        Exercise("Jumping Jack", sets = 2, reps = 30, kcal = 40),
        Exercise("Walking Lunge", sets = 2, reps = 10, kcal = 45),
        Exercise("Low Squat", sets = 2, reps = 12, kcal = 50),
        Exercise("Plank", sets = 2, reps = 30, kcal = 20),
        Exercise("Step-up", sets = 2, reps = 15, kcal = 60),
        Exercise("Marching", sets = 2, reps = 40, kcal = 30),
        Exercise("Wall Push-up", sets = 2, reps = 15, kcal = 35),
        Exercise("Seated Twist", sets = 2, reps = 20, kcal = 25)
    )

    private val restDaysLight = setOf(6, 7, 13, 14, 20, 21, 27, 28)

    private val restDaysHeavy = setOf(3,6,7,10,13,14,17,20,21,24,27,28)

    private fun pickExercises(pool: List<Exercise>, day:Int, weekIndex: Int): List<Exercise> {
        val index = (day-1) % pool.size
        val selectedExercises = mutableListOf<Exercise>()

        for (i in 0 until 3) {
            val currentIndex = (index + i) % pool.size
            val exercises = pool[currentIndex]
            if (weekIndex >= 2) {
                selectedExercises.add(exercises.copy(sets = exercises.sets + 1))
            }
            else {
                selectedExercises.add(exercises)
            }
        }
        return selectedExercises
    }

    fun generatePlan(category: BmiCategory): List<DayPlan> {
        val (pool, restDays) = when (category) {
            BmiCategory.UNDERWEIGHT -> Pair(underweightPool, restDaysLight)
            BmiCategory.NORMAL -> Pair(normalPool, restDaysLight)
            BmiCategory.OVERWEIGHT -> Pair(overweightPool, restDaysHeavy)
        }

       return (1..30).map { day ->
            if (day in restDays) {
                DayPlan(dayNumber = day, isRest = true, exercises = emptyList())
            } else {
                val weekIndex = (day - 1) / 7
                val dailyExercises = pickExercises(pool, day, weekIndex)
                DayPlan(dayNumber = day, isRest = false, exercises = dailyExercises)
            }
        }
    }
}