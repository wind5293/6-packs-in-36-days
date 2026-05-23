package com.example.fitflow.domain

import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.WorkoutExercise
import com.example.fitflow.data.model.FitnessGoal

// ─────────────────────────────────────────────────────────────
// 1. THÊM PLAN MỚI:
//    Tạo một ExercisePool mới và map nó vào planRegistry bên dưới.
//    Chỉ cần làm 2 việc đó, phần còn lại tự hoạt động.
// ─────────────────────────────────────────────────────────────

/** Cấu hình cho một plan: danh sách bài tập + các ngày nghỉ. */
data class ExercisePool(
    val exercises: List<WorkoutExercise>,
    val restDays: Set<Int>
)

/** Helper tạo một WorkoutExercise nhanh hơn, không cần điền đủ tên tham số. */
private fun ex(
    category: String, name: String,
    sets: Int = 3, reps: Int = 0, kcal: Int = 0, durationSec: Int = 60,
    gifs: String = ""
) = WorkoutExercise(category, name, sets, reps, kcal, durationSec, gifs)

// ─────────────────────────────────────────────────────────────
// 2. KHAI BÁO CÁC POOL BÀI TẬP CHO TỪNG MỤC TIÊU
//    → Thêm plan mới: copy một khối bên dưới, đặt tên mới.
// ─────────────────────────────────────────────────────────────

private val WEIGHT_LOSS_POOL = ExercisePool(
    restDays = setOf(6, 7, 13, 14, 20, 21, 27, 28),
    exercises = listOf(
        ex("Cardio",   "Jumping Jack",    reps = 40, kcal = 50),
        ex("Cardio",   "Burpee",          reps = 12, kcal = 80),
        ex("Cardio",   "Mountain Climber",reps = 30, kcal = 60),
        ex("Cardio",   "High Knee",       reps = 30, kcal = 60),
        ex("Cardio",   "Jump Squat",      reps = 15, kcal = 70),
        ex("Cardio",   "Sprint in Place", reps = 30, kcal = 55),
        ex("Cardio",   "Jump Rope",       reps = 50, kcal = 65),
        ex("Cardio",   "Skater Jump",     reps = 20, kcal = 55),
        ex("Cardio",   "Star Jump",       reps = 20, kcal = 45),
    )
)

private val MUSCLE_GAIN_POOL = ExercisePool(
    restDays = setOf(3, 6, 7, 10, 13, 14, 17, 20, 21, 24, 27, 28),
    exercises = listOf(
        ex("Strength", "Push-up",         reps = 12, kcal = 55),
        ex("Strength", "Squat",           reps = 15, kcal = 50),
        ex("Strength", "Lunge",           reps = 12, kcal = 45),
        ex("Strength", "Plank",           reps = 60, kcal = 30),
        ex("Strength", "Pike Push-up",    reps = 10, kcal = 40),
        ex("Strength", "Tricep Dip",      reps = 12, kcal = 45),
        ex("Strength", "Glute Bridge",    reps = 15, kcal = 35),
        ex("Strength", "Diamond Push-up", reps = 10, kcal = 50),
        ex("Strength", "Wall Sit",        reps = 45, kcal = 25),
    )
)

private val ENDURANCE_POOL = ExercisePool(
    restDays = setOf(6, 7, 13, 14, 20, 21, 27, 28),
    exercises = listOf(
        ex("Endurance","Burpee",          reps = 10, kcal = 70),
        ex("Endurance","Push-up",         reps = 12, kcal = 55),
        ex("Endurance","Mountain Climber",reps = 30, kcal = 60),
        ex("Endurance","Jump Squat",      reps = 15, kcal = 65),
        ex("Endurance","Plank",           reps = 60, kcal = 30),
        ex("Endurance","Jumping Jack",    reps = 40, kcal = 50),
        ex("Endurance","High Knee",       reps = 30, kcal = 55),
        ex("Endurance","Lunge",           reps = 12, kcal = 45),
        ex("Endurance","Tricep Dip",      reps = 12, kcal = 40),
    )
)

private val MAINTENANCE_POOL = ExercisePool(
    restDays = setOf(3, 6, 7, 10, 13, 14, 17, 20, 21, 24, 27, 28),
    exercises = listOf(
        ex("Maintenance","Push-up",         reps = 10, kcal = 50),
        ex("Maintenance","Squat",           reps = 12, kcal = 45),
        ex("Maintenance","Jumping Jack",    reps = 30, kcal = 40),
        ex("Maintenance","Plank",           reps = 45, kcal = 25),
        ex("Maintenance","Lunge",           reps = 10, kcal = 40),
        ex("Maintenance","Mountain Climber",reps = 20, kcal = 50),
        ex("Maintenance","Glute Bridge",    reps = 15, kcal = 35),
        ex("Maintenance","High Knee",       reps = 25, kcal = 45),
        ex("Maintenance","Dip",             reps = 10, kcal = 40),
    )
)

// ─────────────────────────────────────────────────────────────
// 3. REGISTRY: Map FitnessGoal → ExercisePool
//    → Thêm plan mới: chỉ thêm 1 dòng vào đây.
// ─────────────────────────────────────────────────────────────

private val planRegistry: Map<FitnessGoal, ExercisePool> = mapOf(
    FitnessGoal.WEIGHT_LOSS to WEIGHT_LOSS_POOL,
    FitnessGoal.MUSCLE_GAIN to MUSCLE_GAIN_POOL,
    FitnessGoal.ENDURANCE   to ENDURANCE_POOL,
    FitnessGoal.MAINTENANCE to MAINTENANCE_POOL,
)

// ─────────────────────────────────────────────────────────────
// 4. ENGINE TẠO LỊCH TẬP — Không cần chỉnh sửa phần này.
// ─────────────────────────────────────────────────────────────

object WorkoutPlanGenerator {

    fun generatePlan(goal: FitnessGoal): List<DayPlan> {
        val pool = planRegistry[goal] ?: planRegistry[FitnessGoal.MAINTENANCE]!!

        return (1..30).map { day ->
            when {
                // TEST override: Ngày 1 dùng dữ liệu thật từ exercises.json
                day == 1 -> DayPlan(
                    dayNumber = 1,
                    isRest = false,
                    workoutExercises = listOf(
                        ex("Chest",     "Band Cross-Over",        sets = 1, reps = 12, kcal = 10, durationSec = 0, gifs = "band_cross-over_1.gif"),
                        ex("Shoulders", "Barbell Shoulder Press", sets = 1, reps = 10, kcal = 15, durationSec = 0, gifs = "barbell_shoulder_press_1.gif"),
                    )
                )
                day in pool.restDays -> DayPlan(
                    dayNumber = day, isRest = true, workoutExercises = emptyList()
                )
                else -> {
                    val weekIndex = (day - 1) / 7
                    DayPlan(
                        dayNumber = day,
                        isRest = false,
                        workoutExercises = pickExercises(pool.exercises, day, weekIndex)
                    )
                }
            }
        }
    }

    private fun pickExercises(
        exercises: List<WorkoutExercise>, day: Int, weekIndex: Int
    ): List<WorkoutExercise> {
        val start = (day - 1) % exercises.size
        return (0 until 3).map { i ->
            val base = exercises[(start + i) % exercises.size]
            if (weekIndex >= 2) base.copy(sets = base.sets + 1) else base
        }
    }
}
