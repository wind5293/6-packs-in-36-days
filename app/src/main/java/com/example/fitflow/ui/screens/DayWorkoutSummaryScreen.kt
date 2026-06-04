package com.example.fitflow.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.R
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.WorkoutExercise
import com.example.fitflow.ui.theme.FitflowTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun DayWorkoutSummaryScreen(
    selectedDate: LocalDate = LocalDate.now(),
    workoutPlan: List<DayPlan> = emptyList(),
    completedDays: Set<Int> = emptySet(),
    currentStreak: Int = 0,
    startDate: LocalDate? = null,
    onBack: () -> Unit = {},
    onNavigateDay: (LocalDate) -> Unit = {},
    onOpenPlanner: () -> Unit = {},
    onOpenWorkoutDay: (Int) -> Unit = {}
) {
    BackHandler(onBack = onBack)

    // Tính dayNumber tương ứng với selectedDate
    val dayNumber: Int? = if (startDate != null) {
        val diff = selectedDate.toEpochDay() - startDate.toEpochDay()
        if (diff in 0..29) (diff + 1).toInt() else null
    } else null

    val dayPlan = dayNumber?.let { dn -> workoutPlan.find { it.dayNumber == dn } }
    val isCompleted = dayNumber != null && dayNumber in completedDays
    val nextPlannedDay = workoutPlan
        .filter { !it.isRest }
        .firstOrNull { it.dayNumber !in completedDays }

    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

    // Tính tổng thống kê
    val totalKcal = dayPlan?.workoutExercises?.sumOf { it.kcal } ?: 0
    val totalMinutes = dayPlan?.workoutExercises?.sumOf { it.durationSec }?.div(60) ?: 0
    val exerciseCount = dayPlan?.workoutExercises?.size ?: 0
    val totalSets = dayPlan?.workoutExercises?.sumOf { it.sets } ?: 0

    // Progress (minutes done vs total)
    val progressFraction = if (isCompleted && totalMinutes > 0) 1f else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Top bar ──────────────────────────────────────────
        item {
            DayWorkoutTopBar(
                selectedDate = selectedDate,
                dateFormatter = dateFormatter,
                onBack = onBack,
                onPrevDay = { onNavigateDay(selectedDate.minusDays(1)) },
                onNextDay = { onNavigateDay(selectedDate.plusDays(1)) }
            )
        }

        // ── Circular Progress + Status ───────────────────────
        item {
            Spacer(Modifier.height(24.dp))
            CircularProgressSection(
                isCompleted = isCompleted,
                isRestDay = dayPlan?.isRest == true,
                noPlan = dayPlan == null,
                animatedProgress = animatedProgress,
                totalMinutes = totalMinutes
            )
        }

        // ── Overview cards ───────────────────────────────────
        item {
            Spacer(Modifier.height(28.dp))
            OverviewSection(
                totalKcal = totalKcal,
                activityCount = totalSets,
                exerciseCount = exerciseCount,
                currentStreak = currentStreak
            )
        }

        item {
            Spacer(Modifier.height(28.dp))
            NextStepSection(
                isCompleted = isCompleted,
                isRestDay = dayPlan?.isRest == true,
                noPlan = dayPlan == null,
                currentDayNumber = dayNumber,
                nextPlannedDayNumber = nextPlannedDay?.dayNumber,
                onOpenPlanner = onOpenPlanner,
                onOpenWorkoutDay = onOpenWorkoutDay,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────

@Composable
private fun DayWorkoutTopBar(
    selectedDate: LocalDate,
    dateFormatter: DateTimeFormatter,
    onBack: () -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                "WORKOUTS",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 3.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevDay,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Previous day",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                selectedDate.format(dateFormatter),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(
                onClick = onNextDay,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next day",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f))
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Circular Progress Section
// ─────────────────────────────────────────────────────────────

@Composable
private fun CircularProgressSection(
    isCompleted: Boolean,
    isRestDay: Boolean,
    noPlan: Boolean,
    animatedProgress: Float,
    totalMinutes: Int
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f)

    val emoji = when {
        noPlan -> "😴"
        isRestDay -> "🧘"
        isCompleted -> "🏆"
        else -> "😊"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                val inset = strokeWidth / 2f

                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth)
                )

                if (!noPlan && !isRestDay) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(primaryColor, secondaryColor, primaryColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                        size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth)
                    )
                }
            }

            Text(emoji, fontSize = 44.sp)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (isCompleted) totalMinutes.toString() else "0",
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            fontSize = 42.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = when {
                noPlan -> "No plan for this day"
                isRestDay -> "Rest day"
                else -> "/$totalMinutes min"
            },
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        )

        Spacer(Modifier.height(12.dp))

        val (pillText, pillColor) = when {
            noPlan -> "OUT OF PLAN" to MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            isRestDay -> "REST DAY" to MaterialTheme.colorScheme.secondary
            isCompleted -> "COMPLETED" to MaterialTheme.colorScheme.primary
            else -> "PENDING" to MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(pillColor.copy(alpha = 0.12f))
                .border(1.dp, pillColor.copy(alpha = 0.3f), RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                pillText,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = pillColor
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Overview Section — 2×2 stat grid
// ─────────────────────────────────────────────────────────────

@Composable
private fun OverviewSection(
    totalKcal: Int,
    activityCount: Int,
    exerciseCount: Int,
    currentStreak: Int
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            "Overview",
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OverviewStatCard(
                modifier = Modifier.weight(1f),
                label = "Total",
                value = "$totalKcal Kcal",
                icon = Icons.Default.LocalFireDepartment,
                iconTint = MaterialTheme.colorScheme.primary
            )
            OverviewStatCard(
                modifier = Modifier.weight(1f),
                label = "Activity",
                value = activityCount.toString(),
                icon = Icons.Default.FitnessCenter,
                iconTint = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OverviewStatCard(
                modifier = Modifier.weight(1f),
                label = "Exercises",
                value = exerciseCount.toString(),
                icon = Icons.Default.Schedule,
                iconTint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            OverviewStatCard(
                modifier = Modifier.weight(1f),
                label = "Streak",
                value = "$currentStreak Day",
                icon = Icons.Default.Whatshot,
                iconTint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun NextStepSection(
    isCompleted: Boolean,
    isRestDay: Boolean,
    noPlan: Boolean,
    currentDayNumber: Int?,
    nextPlannedDayNumber: Int?,
    onOpenPlanner: () -> Unit,
    onOpenWorkoutDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val titleRes = when {
        noPlan -> R.string.day_summary_title_out_of_plan
        isRestDay -> R.string.day_summary_title_rest_day
        isCompleted -> R.string.day_summary_title_complete
        else -> R.string.day_summary_title_pending
    }
    val bodyRes = when {
        noPlan -> R.string.day_summary_body_out_of_plan
        isRestDay -> R.string.day_summary_body_rest_day
        isCompleted -> R.string.day_summary_body_complete
        else -> R.string.day_summary_body_pending
    }

    val primaryAction = when {
        nextPlannedDayNumber != null && nextPlannedDayNumber != currentDayNumber -> {
            Pair(
                stringResource(R.string.day_summary_primary_next_day_format, nextPlannedDayNumber),
                { onOpenWorkoutDay(nextPlannedDayNumber) }
            )
        }
        !noPlan && currentDayNumber != null && !isCompleted && !isRestDay -> {
            Pair(
                stringResource(R.string.day_summary_primary_resume_today),
                { onOpenWorkoutDay(currentDayNumber) }
            )
        }
        else -> {
            Pair(stringResource(R.string.day_summary_primary_view_planner), onOpenPlanner)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(titleRes),
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(bodyRes),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )
            Button(
                onClick = primaryAction.second,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = primaryAction.first,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            OutlinedButton(
                onClick = onOpenPlanner,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = stringResource(R.string.day_summary_secondary_back_to_planner),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun OverviewStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color
) {
    Card(
        modifier = modifier.border(
            1.dp,
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f),
            RoundedCornerShape(20.dp)
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                value,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun DayWorkoutSummaryScreenPreview() {
    FitflowTheme {
        DayWorkoutSummaryScreen(
            selectedDate = LocalDate.now(),
            workoutPlan = listOf(
                DayPlan(
                    dayNumber = 1,
                    isRest = false,
                    title = "Upper Body Blast",
                    difficulty = "Intermediate",
                    muscleGroup = "Chest & Arms",
                    workoutExercises = listOf(
                        WorkoutExercise("Strength", "Push-Up", 3, 15, 45, 60),
                        WorkoutExercise("Cardio", "Jumping Jacks", 3, 30, 60, 90),
                    )
                )
            ),
            completedDays = emptySet(),
            currentStreak = 3,
            startDate = LocalDate.now()
        )
    }
}
