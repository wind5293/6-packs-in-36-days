package com.example.fitflow.domain

import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.WorkoutExercise

// ─────────────────────────────────────────────────────────────
// JEFIT "From Fat to Fit – 3 Month Plan" – MONTH 1 MOCK DATA
// Cấu trúc: 6 ngày tập / tuần, 1 ngày nghỉ (Sunday).
// Mỗi ngày tập tập trung 1 nhóm cơ chính + Cardio warm-up + Core finisher.
// ─────────────────────────────────────────────────────────────

private fun ex(
    category: String,
    name: String,
    sets: Int = 3,
    reps: Int = 0,
    kcal: Int = 0,
    durationSec: Int = 0,
    desc: String = ""
) = WorkoutExercise(
    category = category,
    name = name,
    sets = sets,
    reps = reps,
    kcal = kcal,
    durationSec = durationSec,
    gifFileName = "",
    description = desc
)

// ── WEEK 1 ──────────────────────────────────────────────────

// Day 1 – Chest & Triceps
private val day1Exercises = listOf(
    ex("Cardio",   "Jumping Jack",           sets=1, reps=0,  kcal=30, durationSec=60,  desc="Warm-up. Keep a steady rhythm, arms fully extended overhead."),
    ex("Cardio",   "Treadmill Jog",          sets=1, reps=0,  kcal=80, durationSec=300, desc="Moderate pace 5–6 km/h to elevate heart rate before lifting."),
    ex("Chest",    "Flat Barbell Bench Press",sets=3, reps=12, kcal=55, desc="Plant feet flat. Lower bar to mid-chest, press explosively."),
    ex("Chest",    "Incline Dumbbell Press", sets=3, reps=12, kcal=50, desc="30–45° incline. Control the descent, squeeze chest at top."),
    ex("Chest",    "Dumbbell Flyes",         sets=3, reps=12, kcal=45, desc="Slight elbow bend. Feel the chest stretch at the bottom."),
    ex("Triceps",  "Tricep Pushdown",        sets=3, reps=12, kcal=35, desc="Cable or band. Keep elbows pinned at sides, full extension."),
    ex("Triceps",  "Skull Crusher",          sets=3, reps=10, kcal=40, desc="EZ-bar or dumbbells. Lower slowly to forehead, press up."),
    ex("Core",     "Plank Hold",             sets=3, reps=0,  kcal=20, durationSec=45,  desc="Neutral spine, engage core. Don't let hips sag.")
)

// Day 2 – Back & Biceps
private val day2Exercises = listOf(
    ex("Cardio",   "Jump Rope",              sets=1, reps=0,  kcal=40, durationSec=120, desc="Continuous skipping. Light on feet, wrists do the work."),
    ex("Back",     "Pull-Up",                sets=3, reps=8,  kcal=60, desc="Full ROM, dead hang to chin over bar. Use assisted machine if needed."),
    ex("Back",     "Barbell Bent-Over Row",  sets=3, reps=12, kcal=65, desc="Hinge at hips ~45°, pull bar to lower chest, retract scapula."),
    ex("Back",     "Lat Pulldown",           sets=3, reps=12, kcal=50, desc="Wide grip, lean slightly back, drive elbows down to lats."),
    ex("Back",     "Seated Cable Row",       sets=3, reps=12, kcal=45, desc="Keep chest tall, pull handle to navel, squeeze mid-back."),
    ex("Biceps",   "Barbell Curl",           sets=3, reps=12, kcal=35, desc="No swinging. Control eccentric, full supination at top."),
    ex("Biceps",   "Hammer Curl",            sets=3, reps=12, kcal=30, desc="Neutral grip. Works brachialis for thicker arms."),
    ex("Core",     "Hanging Leg Raise",      sets=3, reps=12, kcal=30, desc="Legs straight or bent. Avoid momentum, control the descent.")
)

// Day 3 – Legs
private val day3Exercises = listOf(
    ex("Cardio",   "Stationary Bike",        sets=1, reps=0,  kcal=90, durationSec=300, desc="Moderate resistance, steady cadence for a thorough warm-up."),
    ex("Legs",     "Barbell Back Squat",     sets=4, reps=12, kcal=80, desc="Hip-width stance. Break parallel if mobility allows, drive through heels."),
    ex("Legs",     "Leg Press",              sets=3, reps=15, kcal=70, desc="Feet shoulder-width. Full ROM without locking knees at top."),
    ex("Legs",     "Romanian Deadlift",      sets=3, reps=12, kcal=65, desc="Hinge at hips, bar close to shins. Feel hamstring stretch."),
    ex("Legs",     "Leg Curl",               sets=3, reps=12, kcal=40, desc="Lying or seated. Control the movement, full range."),
    ex("Legs",     "Calf Raise",             sets=4, reps=20, kcal=25, desc="Full stretch at bottom, squeeze hard at top."),
    ex("Core",     "Crunch",                 sets=3, reps=20, kcal=20, desc="Curl upper back off floor. Exhale on contraction.")
)

// Day 4 – Shoulders
private val day4Exercises = listOf(
    ex("Cardio",   "High Knee",              sets=1, reps=0,  kcal=35, durationSec=60,  desc="Drive knees to hip height. Pump arms to boost heart rate."),
    ex("Cardio",   "Elliptical",             sets=1, reps=0,  kcal=85, durationSec=300, desc="Full-body low-impact warm-up. Moderate resistance."),
    ex("Shoulders","Overhead Barbell Press", sets=4, reps=10, kcal=60, desc="Standing or seated. Press bar from clavicle to full lockout."),
    ex("Shoulders","Dumbbell Lateral Raise", sets=3, reps=15, kcal=35, desc="Slight forward lean. Raise to shoulder height, lead with elbows."),
    ex("Shoulders","Front Plate Raise",      sets=3, reps=12, kcal=30, desc="Arms nearly straight. Raise plate to eye level, lower slowly."),
    ex("Shoulders","Face Pull",              sets=3, reps=15, kcal=30, desc="Rope attachment at head height. Pull to face, external rotation."),
    ex("Traps",    "Barbell Shrug",          sets=3, reps=15, kcal=35, desc="Straight arms. Shrug up and hold 1 second at peak contraction."),
    ex("Core",     "Side Plank",             sets=3, reps=0,  kcal=15, durationSec=30,  desc="30 sec each side. Stack feet or stagger for balance.")
)

// Day 5 – Core & Cardio
private val day5Exercises = listOf(
    ex("Cardio",   "Burpee",                 sets=3, reps=10, kcal=80, desc="Full-body explosive movement. Control landing."),
    ex("Cardio",   "Mountain Climber",       sets=3, reps=0,  kcal=55, durationSec=30,  desc="High-plank position. Drive knees alternately at pace."),
    ex("Core",     "Cable Crunch",           sets=3, reps=15, kcal=30, desc="Kneel in front of cable. Round spine, don't pull with arms."),
    ex("Core",     "Leg Raise",              sets=3, reps=12, kcal=28, desc="Flat bench or floor. Lower legs to just above ground."),
    ex("Core",     "Russian Twist",          sets=3, reps=20, kcal=32, desc="Hold a plate or medicine ball. Rotate fully each side."),
    ex("Core",     "Ab Rollout",             sets=3, reps=10, kcal=40, desc="Roll out fully, brace hard to roll back. Beginner: use knees."),
    ex("Cardio",   "Treadmill Sprint",       sets=5, reps=0,  kcal=120,durationSec=30,  desc="5 × 30s max effort, 30s rest. All-out intensity.")
)

// Day 6 – Full Body Cardio Circuit
private val day6Exercises = listOf(
    ex("Cardio",   "Jump Squat",             sets=3, reps=15, kcal=70, desc="Squat deep, explode upward, soft landing."),
    ex("Cardio",   "Push-Up",                sets=3, reps=15, kcal=55, desc="Chest touches floor. Full lockout at top."),
    ex("Cardio",   "Lunge Jump",             sets=3, reps=12, kcal=65, desc="Alternate legs mid-air. Land softly in lunge position."),
    ex("Cardio",   "Skater Jump",            sets=3, reps=20, kcal=55, desc="Side-to-side lateral bounding. Reach hand to opposite foot."),
    ex("Core",     "Plank to Downward Dog",  sets=3, reps=12, kcal=30, desc="From high plank push back to downward dog. Controlled."),
    ex("Cardio",   "Battle Rope Wave",       sets=3, reps=0,  kcal=90, durationSec=30,  desc="Alternate arm waves. Keep hips low, shoulders back."),
    ex("Cardio",   "Box Jump",               sets=3, reps=10, kcal=60, desc="Explosive jump onto box. Step down carefully each time.")
)

// Day 7 – REST
// ── WEEK 2 (Progressive overload: +2 reps per exercise) ──

private val day8Exercises = listOf(
    ex("Cardio",   "Jumping Jack",           sets=1, reps=0,  kcal=30, durationSec=60),
    ex("Cardio",   "Treadmill Jog",          sets=1, reps=0,  kcal=80, durationSec=300),
    ex("Chest",    "Flat Barbell Bench Press",sets=3, reps=14, kcal=60),
    ex("Chest",    "Incline Dumbbell Press", sets=3, reps=14, kcal=55),
    ex("Chest",    "Cable Crossover",        sets=3, reps=14, kcal=50, desc="Low-to-high cables. Squeeze at centre of chest."),
    ex("Triceps",  "Tricep Overhead Extension",sets=3,reps=12,kcal=38, desc="Keep elbows close to head. Full extension overhead."),
    ex("Triceps",  "Close-Grip Bench Press", sets=3, reps=10, kcal=45, desc="Narrower grip, elbows in. Heavy compound tricep movement."),
    ex("Core",     "Plank Hold",             sets=3, reps=0,  kcal=20, durationSec=50)
)

private val day9Exercises = listOf(
    ex("Cardio",   "Jump Rope",              sets=1, reps=0,  kcal=40, durationSec=120),
    ex("Back",     "Pull-Up",                sets=3, reps=10, kcal=65, desc="Add weight if 10 reps is easy."),
    ex("Back",     "T-Bar Row",              sets=3, reps=12, kcal=65, desc="Chest support preferred. Drive elbows back, full squeeze."),
    ex("Back",     "Lat Pulldown",           sets=3, reps=14, kcal=55),
    ex("Back",     "One-Arm Dumbbell Row",   sets=3, reps=12, kcal=50, desc="Support on bench. Full stretch at bottom."),
    ex("Biceps",   "Incline Dumbbell Curl",  sets=3, reps=12, kcal=35, desc="Arms hang, great stretch. Curl without swinging."),
    ex("Biceps",   "Concentration Curl",     sets=3, reps=12, kcal=28, desc="Elbow on inner thigh. Squeeze bicep hard at top."),
    ex("Core",     "Hanging Leg Raise",      sets=3, reps=14, kcal=32)
)

private val day10Exercises = listOf(
    ex("Cardio",   "Stationary Bike",        sets=1, reps=0,  kcal=90, durationSec=300),
    ex("Legs",     "Barbell Back Squat",     sets=4, reps=14, kcal=85),
    ex("Legs",     "Hack Squat",             sets=3, reps=14, kcal=72, desc="Feet forward on plate. Targets quads strongly."),
    ex("Legs",     "Romanian Deadlift",      sets=3, reps=14, kcal=70),
    ex("Legs",     "Lying Leg Curl",         sets=3, reps=14, kcal=42),
    ex("Legs",     "Seated Calf Raise",      sets=4, reps=20, kcal=25),
    ex("Core",     "Decline Sit-Up",         sets=3, reps=20, kcal=28, desc="Hands behind head. Don't pull neck.")
)

private val day11Exercises = listOf(
    ex("Cardio",   "High Knee",              sets=1, reps=0,  kcal=35, durationSec=60),
    ex("Cardio",   "Elliptical",             sets=1, reps=0,  kcal=85, durationSec=300),
    ex("Shoulders","Arnold Press",           sets=4, reps=12, kcal=65, desc="Rotate from palms-facing you to away as you press. Works all 3 heads."),
    ex("Shoulders","Dumbbell Lateral Raise", sets=3, reps=17, kcal=38),
    ex("Shoulders","Bent-Over Rear Delt Fly",sets=3, reps=15, kcal=32, desc="Hinge 45°. Raise arms laterally, pinch rear delts."),
    ex("Shoulders","Cable Lateral Raise",    sets=3, reps=15, kcal=30, desc="Low pulley, arm across body. Great constant-tension movement."),
    ex("Traps",    "Dumbbell Shrug",         sets=3, reps=15, kcal=32),
    ex("Core",     "Side Plank",             sets=3, reps=0,  kcal=18, durationSec=35)
)

private val day12Exercises = listOf(
    ex("Cardio",   "Burpee",                 sets=3, reps=12, kcal=90),
    ex("Cardio",   "Mountain Climber",       sets=3, reps=0,  kcal=60, durationSec=35),
    ex("Core",     "Cable Crunch",           sets=3, reps=17, kcal=34),
    ex("Core",     "Reverse Crunch",         sets=3, reps=15, kcal=28, desc="Hips curl upward. Slow eccentric."),
    ex("Core",     "Russian Twist",          sets=3, reps=24, kcal=35),
    ex("Core",     "Dragon Flag",            sets=3, reps=6,  kcal=45, desc="Advanced. Keep body rigid. Lower slow."),
    ex("Cardio",   "Treadmill Sprint",       sets=6, reps=0,  kcal=140,durationSec=30)
)

private val day13Exercises = listOf(
    ex("Cardio",   "Jump Squat",             sets=3, reps=17, kcal=75),
    ex("Cardio",   "Push-Up",                sets=3, reps=17, kcal=60),
    ex("Cardio",   "Lunge Jump",             sets=3, reps=14, kcal=70),
    ex("Cardio",   "Lateral Shuffle",        sets=3, reps=0,  kcal=55, durationSec=30, desc="Quick side-to-side steps. Stay low, light feet."),
    ex("Core",     "Hollow Body Hold",       sets=3, reps=0,  kcal=28, durationSec=30, desc="Arms overhead, lower back pressed to floor."),
    ex("Cardio",   "Battle Rope Wave",       sets=3, reps=0,  kcal=95, durationSec=35),
    ex("Cardio",   "Box Jump",               sets=3, reps=12, kcal=65)
)

// ── WEEK 3 (Sets +1 for primary lifts) ──

private val day15Exercises = listOf(
    ex("Cardio",   "Jumping Jack",           sets=1, reps=0,  kcal=30, durationSec=60),
    ex("Chest",    "Flat Barbell Bench Press",sets=4, reps=12, kcal=65),
    ex("Chest",    "Incline Dumbbell Press", sets=4, reps=12, kcal=58),
    ex("Chest",    "Dumbbell Flyes",         sets=3, reps=14, kcal=48),
    ex("Chest",    "Push-Up to Failure",     sets=2, reps=0,  kcal=55, durationSec=60, desc="Max reps each set. Note your count."),
    ex("Triceps",  "Tricep Pushdown",        sets=4, reps=14, kcal=40),
    ex("Triceps",  "Overhead Extension",     sets=3, reps=12, kcal=38),
    ex("Core",     "Plank Hold",             sets=3, reps=0,  kcal=22, durationSec=60)
)

private val day16Exercises = listOf(
    ex("Cardio",   "Jump Rope",              sets=1, reps=0,  kcal=50, durationSec=180),
    ex("Back",     "Pull-Up",                sets=4, reps=10, kcal=70),
    ex("Back",     "Barbell Bent-Over Row",  sets=4, reps=12, kcal=72),
    ex("Back",     "Lat Pulldown",           sets=3, reps=14, kcal=55),
    ex("Back",     "Seated Cable Row",       sets=3, reps=14, kcal=50),
    ex("Biceps",   "Barbell Curl",           sets=4, reps=12, kcal=38),
    ex("Biceps",   "Preacher Curl",          sets=3, reps=12, kcal=32, desc="Strict form, full range. Great biceps peak builder."),
    ex("Core",     "Hanging Knee Raise",     sets=3, reps=15, kcal=30)
)

private val day17Exercises = listOf(
    ex("Cardio",   "Stationary Bike",        sets=1, reps=0,  kcal=100,durationSec=300),
    ex("Legs",     "Barbell Back Squat",     sets=5, reps=12, kcal=95),
    ex("Legs",     "Leg Press",              sets=4, reps=15, kcal=80),
    ex("Legs",     "Romanian Deadlift",      sets=4, reps=12, kcal=72),
    ex("Legs",     "Leg Extension",          sets=3, reps=15, kcal=42, desc="Full extension, hold 1 sec at top."),
    ex("Legs",     "Calf Raise",             sets=4, reps=25, kcal=28),
    ex("Core",     "Crunch",                 sets=3, reps=25, kcal=25)
)

private val day18Exercises = listOf(
    ex("Cardio",   "High Knee",              sets=1, reps=0,  kcal=40, durationSec=60),
    ex("Shoulders","Overhead Barbell Press", sets=4, reps=12, kcal=65),
    ex("Shoulders","Dumbbell Lateral Raise", sets=4, reps=15, kcal=38),
    ex("Shoulders","Front Dumbbell Raise",   sets=3, reps=12, kcal=32, desc="Alternating arms. Keep slight elbow bend."),
    ex("Shoulders","Face Pull",              sets=4, reps=15, kcal=32),
    ex("Shoulders","Upright Row",            sets=3, reps=12, kcal=40, desc="EZ-bar or dumbbells. Pull to chin, elbows high."),
    ex("Core",     "Side Plank",             sets=3, reps=0,  kcal=20, durationSec=40)
)

private val day19Exercises = listOf(
    ex("Cardio",   "Burpee",                 sets=4, reps=12, kcal=100),
    ex("Cardio",   "Mountain Climber",       sets=4, reps=0,  kcal=65, durationSec=40),
    ex("Core",     "Cable Crunch",           sets=4, reps=15, kcal=36),
    ex("Core",     "Leg Raise",              sets=3, reps=15, kcal=30),
    ex("Core",     "Russian Twist",          sets=3, reps=30, kcal=38),
    ex("Core",     "Ab Rollout",             sets=3, reps=12, kcal=45),
    ex("Cardio",   "Treadmill Sprint",       sets=6, reps=0,  kcal=150,durationSec=30)
)

private val day20Exercises = listOf(
    ex("Cardio",   "Jump Squat",             sets=4, reps=15, kcal=80),
    ex("Cardio",   "Push-Up",                sets=4, reps=15, kcal=65),
    ex("Cardio",   "Lunge Jump",             sets=4, reps=12, kcal=75),
    ex("Cardio",   "Skater Jump",            sets=3, reps=24, kcal=62),
    ex("Core",     "Plank to Downward Dog",  sets=3, reps=14, kcal=35),
    ex("Cardio",   "Battle Rope Wave",       sets=4, reps=0,  kcal=105,durationSec=35),
    ex("Cardio",   "Box Jump",               sets=4, reps=10, kcal=70)
)

// ── WEEK 4 (Deload / Peak – shorter sets, higher intensity) ──

private val day22Exercises = listOf(
    ex("Cardio",   "Jumping Jack",           sets=1, reps=0,  kcal=30, durationSec=60),
    ex("Chest",    "Flat Barbell Bench Press",sets=3, reps=8,  kcal=70, desc="Heavy weight deload week – fewer reps, more weight."),
    ex("Chest",    "Incline Dumbbell Press", sets=3, reps=8,  kcal=62),
    ex("Chest",    "Cable Crossover",        sets=3, reps=12, kcal=52),
    ex("Triceps",  "Close-Grip Bench Press", sets=3, reps=8,  kcal=50),
    ex("Triceps",  "Tricep Pushdown",        sets=3, reps=15, kcal=38),
    ex("Core",     "Plank Hold",             sets=3, reps=0,  kcal=22, durationSec=60)
)

private val day23Exercises = listOf(
    ex("Cardio",   "Jump Rope",              sets=1, reps=0,  kcal=50, durationSec=180),
    ex("Back",     "Deadlift",               sets=3, reps=5,  kcal=90, desc="Peak week. Heavy, controlled. Drive through floor."),
    ex("Back",     "Barbell Bent-Over Row",  sets=3, reps=8,  kcal=72),
    ex("Back",     "Lat Pulldown",           sets=3, reps=12, kcal=55),
    ex("Biceps",   "Barbell Curl",           sets=3, reps=10, kcal=38),
    ex("Biceps",   "Hammer Curl",            sets=3, reps=12, kcal=32),
    ex("Core",     "Hanging Leg Raise",      sets=3, reps=15, kcal=32)
)

private val day24Exercises = listOf(
    ex("Cardio",   "Stationary Bike",        sets=1, reps=0,  kcal=100,durationSec=300),
    ex("Legs",     "Barbell Back Squat",     sets=4, reps=8,  kcal=90, desc="Heavy sets. Good form above all."),
    ex("Legs",     "Romanian Deadlift",      sets=3, reps=10, kcal=72),
    ex("Legs",     "Leg Curl",               sets=3, reps=12, kcal=42),
    ex("Legs",     "Calf Raise",             sets=4, reps=20, kcal=28),
    ex("Core",     "Crunch",                 sets=3, reps=20, kcal=22)
)

private val day25Exercises = listOf(
    ex("Cardio",   "High Knee",              sets=1, reps=0,  kcal=40, durationSec=60),
    ex("Shoulders","Overhead Barbell Press", sets=3, reps=8,  kcal=68, desc="Heavy press. Control the eccentric."),
    ex("Shoulders","Dumbbell Lateral Raise", sets=4, reps=15, kcal=40),
    ex("Shoulders","Face Pull",              sets=3, reps=15, kcal=32),
    ex("Shoulders","Rear Delt Fly",          sets=3, reps=15, kcal=30),
    ex("Core",     "Side Plank",             sets=3, reps=0,  kcal=20, durationSec=45)
)

private val day26Exercises = listOf(
    ex("Cardio",   "Burpee",                 sets=4, reps=15, kcal=120),
    ex("Core",     "Cable Crunch",           sets=4, reps=15, kcal=36),
    ex("Core",     "Dragon Flag",            sets=3, reps=8,  kcal=50),
    ex("Core",     "Russian Twist",          sets=3, reps=30, kcal=38),
    ex("Cardio",   "Treadmill Sprint",       sets=8, reps=0,  kcal=180,durationSec=30, desc="Final intensity peak. Max effort each sprint.")
)

private val day27Exercises = listOf(
    ex("Cardio",   "Full Body Circuit",      sets=4, reps=0,  kcal=200,durationSec=600, desc="30-min full-body circuit at moderate intensity. Month 1 finale."),
    ex("Core",     "Plank Hold",             sets=3, reps=0,  kcal=22, durationSec=60),
    ex("Core",     "Side Plank",             sets=3, reps=0,  kcal=18, durationSec=40),
    ex("Core",     "Ab Rollout",             sets=3, reps=12, kcal=45)
)

// ─────────────────────────────────────────────────────────────
// PUBLIC FACTORY FUNCTION
// ─────────────────────────────────────────────────────────────

fun buildJefitMonth1Plan(): List<DayPlan> = listOf(
    // WEEK 1
    DayPlan(1,  false, day1Exercises,  "Chest & Triceps", "Intermediate", "Chest"),
    DayPlan(2,  false, day2Exercises,  "Back & Biceps",   "Intermediate", "Back"),
    DayPlan(3,  false, day3Exercises,  "Leg Day",         "Intermediate", "Legs"),
    DayPlan(4,  false, day4Exercises,  "Shoulders",       "Intermediate", "Shoulders"),
    DayPlan(5,  false, day5Exercises,  "Core & Cardio",   "Intermediate", "Core"),
    DayPlan(6,  false, day6Exercises,  "Full Body Circuit","Intermediate","Full Body"),
    DayPlan(7,  true,  emptyList(),   "Rest & Recovery", "Easy",         "Rest"),
    // WEEK 2
    DayPlan(8,  false, day8Exercises,  "Chest & Triceps", "Intermediate", "Chest"),
    DayPlan(9,  false, day9Exercises,  "Back & Biceps",   "Intermediate", "Back"),
    DayPlan(10, false, day10Exercises, "Leg Day",         "Intermediate", "Legs"),
    DayPlan(11, false, day11Exercises, "Shoulders",       "Intermediate", "Shoulders"),
    DayPlan(12, false, day12Exercises, "Core & Cardio",   "Intermediate", "Core"),
    DayPlan(13, false, day13Exercises, "Full Body Circuit","Intermediate","Full Body"),
    DayPlan(14, true,  emptyList(),   "Rest & Recovery", "Easy",         "Rest"),
    // WEEK 3
    DayPlan(15, false, day15Exercises, "Chest & Triceps", "Advanced",    "Chest"),
    DayPlan(16, false, day16Exercises, "Back & Biceps",   "Advanced",    "Back"),
    DayPlan(17, false, day17Exercises, "Leg Day",         "Advanced",    "Legs"),
    DayPlan(18, false, day18Exercises, "Shoulders",       "Advanced",    "Shoulders"),
    DayPlan(19, false, day19Exercises, "Core & Cardio",   "Advanced",    "Core"),
    DayPlan(20, false, day20Exercises, "Full Body Circuit","Advanced",   "Full Body"),
    DayPlan(21, true,  emptyList(),   "Rest & Recovery", "Easy",         "Rest"),
    // WEEK 4
    DayPlan(22, false, day22Exercises, "Chest & Triceps", "Advanced",    "Chest"),
    DayPlan(23, false, day23Exercises, "Back & Biceps",   "Advanced",    "Back"),
    DayPlan(24, false, day24Exercises, "Leg Day",         "Advanced",    "Legs"),
    DayPlan(25, false, day25Exercises, "Shoulders",       "Advanced",    "Shoulders"),
    DayPlan(26, false, day26Exercises, "Core & Cardio",   "Advanced",    "Core"),
    DayPlan(27, false, day27Exercises, "Full Body Finale","Advanced",    "Full Body"),
    DayPlan(28, true,  emptyList(),   "Rest & Recovery", "Easy",         "Rest"),
    // Days 29-30 (bonus repeat)
    DayPlan(29, false, day1Exercises,  "Chest & Triceps", "Advanced",    "Chest"),
    DayPlan(30, false, day2Exercises,  "Back & Biceps",   "Advanced",    "Back"),
)
