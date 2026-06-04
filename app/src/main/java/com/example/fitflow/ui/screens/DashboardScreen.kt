package com.example.fitflow.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.fitflow.data.model.UserProfile
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import com.composables.icons.lucide.Footprints
import com.composables.icons.lucide.GlassWater
import com.composables.icons.lucide.Lucide
import com.example.fitflow.R
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.DailyHealthMetrics
import com.example.fitflow.data.model.StepSource
import com.example.fitflow.ui.theme.FitflowTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    completedDays: Set<Int> = emptySet(),
    currentStreak: Int = 0,
    workoutPlan: List<DayPlan> = emptyList(),
    userProfile: UserProfile? = null,
    healthMetrics: DailyHealthMetrics = DailyHealthMetrics(LocalDate.now(), 0, 0, 2000, StepSource.MANUAL),
    isActivityRecognitionGranted: Boolean = false,
    isStepSensorEnabled: Boolean = false,
    isStepTrackingActive: Boolean = false,
    onUnlockStepSensor: () -> Unit = {},
    onAddWater: (Int) -> Unit = {},
    onSetWaterGoal: (Int) -> Unit = {},
    onStartWorkout: () -> Unit = {},
    onOpenNextWorkout: (Int) -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val totalWorkoutDays = workoutPlan.count { !it.isRest }
    val completedCount = completedDays.size
    val totalKcal = workoutPlan
        .filter { it.dayNumber in completedDays }
        .flatMap { it.workoutExercises }
        .sumOf { it.kcal }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = 16.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = paddingValues.calculateBottomPadding() + 16.dp,
        )
    ) {
        item {
            HeaderSection(onOpenSettings = onOpenSettings)
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
        item {
            WeeklyGoalSection(
                workoutPlan = workoutPlan,
                completedDays = completedDays
            )
        }
        item {
            if (userProfile != null) {
                TodayWeightSection(userProfile = userProfile)
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            NextWorkoutSection(
                workoutPlan = workoutPlan,
                completedDays = completedDays,
                onOpenNextWorkout = onOpenNextWorkout,
                onOpenPlanner = onStartWorkout
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            CheckInRecordSection(
                workoutPlan = workoutPlan,
                completedDays = completedDays,
                currentStreak = currentStreak
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            WorkoutsSummarySection(
                completedCount = completedCount,
                totalWorkoutDays = totalWorkoutDays,
                totalKcal = totalKcal,
                onStartWorkout = onStartWorkout
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            HealthMetricsSection(
                metrics = healthMetrics,
                isActivityRecognitionGranted = isActivityRecognitionGranted,
                isStepSensorEnabled = isStepSensorEnabled,
                isStepTrackingActive = isStepTrackingActive,
                onUnlockStepSensor = onUnlockStepSensor,
                onAddWater = onAddWater,
                onSetWaterGoal = onSetWaterGoal
            )
        }
    }
}

@Composable
fun TodayWeightSection(userProfile: UserProfile) {
    val weightLeft = userProfile.weight - userProfile.targetWeight
    val emoji = when {
        weightLeft > 5 -> "🔥"
        weightLeft > 0 -> "💪"
        else -> "🎉"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.dashboard_today_weight),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.dashboard_weight_kg_format, userProfile.weight),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        emoji,
                        fontSize = 24.sp
                    )
                }
                Text(
                    stringResource(R.string.dashboard_weight_to_goal_format, weightLeft),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    stringResource(R.string.dashboard_updated_today),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun NextWorkoutSection(
    workoutPlan: List<DayPlan>,
    completedDays: Set<Int>,
    onOpenNextWorkout: (Int) -> Unit,
    onOpenPlanner: () -> Unit
) {
    val completedWorkoutCount = workoutPlan.count { !it.isRest && it.dayNumber in completedDays }
    val totalWorkoutCount = workoutPlan.count { !it.isRest }
    val nextWorkoutDay = workoutPlan.firstOrNull { !it.isRest && it.dayNumber !in completedDays }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboard_next_workout_label),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            if (nextWorkoutDay != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(
                                R.string.dashboard_next_workout_day_format,
                                nextWorkoutDay.dayNumber
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic
                        )
                        Text(
                            text = if (nextWorkoutDay.title.isNotBlank()) nextWorkoutDay.title else nextWorkoutDay.muscleGroup.ifBlank { stringResource(R.string.dashboard_next_workout_default_title) },
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(
                                R.string.dashboard_next_workout_progress_format,
                                completedWorkoutCount,
                                totalWorkoutCount
                            ),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (nextWorkoutDay.isRest) "R" else nextWorkoutDay.dayNumber.toString(),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Text(
                    text = stringResource(
                        R.string.dashboard_next_workout_meta_format,
                        nextWorkoutDay.workoutExercises.sumOf { it.durationSec } / 60,
                        nextWorkoutDay.workoutExercises.sumOf { it.kcal }
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = { onOpenNextWorkout(nextWorkoutDay.dayNumber) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.dashboard_next_workout_open_day_format, nextWorkoutDay.dayNumber),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            } else {
                Text(
                    text = if (totalWorkoutCount == 0) {
                        stringResource(R.string.dashboard_next_workout_empty_title)
                    } else {
                        stringResource(R.string.dashboard_plan_complete_title)
                    },
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic
                )
                Text(
                    text = if (totalWorkoutCount == 0) {
                        stringResource(R.string.dashboard_next_workout_empty_body)
                    } else {
                        stringResource(
                            R.string.dashboard_plan_complete_body,
                            completedWorkoutCount,
                            totalWorkoutCount
                        )
                    },
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Button(
                    onClick = onOpenPlanner,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (totalWorkoutCount == 0) {
                            stringResource(R.string.dashboard_next_workout_empty_button)
                        } else {
                            stringResource(R.string.dashboard_plan_complete_button)
                        },
                        color = MaterialTheme.colorScheme.background,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(onOpenSettings: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.dashboard_status_report),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Row {
                Text(
                    stringResource(R.string.dashboard_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic
                )
            }
        }
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                    RoundedCornerShape(50)
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                    RoundedCornerShape(50)
                )
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = stringResource(R.string.common_settings),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun CheckInRecordSection(
    workoutPlan: List<DayPlan>,
    completedDays: Set<Int>,
    currentStreak: Int
) {
    val workoutDays = workoutPlan.filter { !it.isRest }
    val currentWorkoutDay = workoutDays.firstOrNull { it.dayNumber !in completedDays }?.dayNumber
        ?: workoutDays.lastOrNull()?.dayNumber
    val currentWorkoutIndex = workoutDays.indexOfFirst { it.dayNumber == currentWorkoutDay }.coerceAtLeast(0)
    val recentWorkoutDays = workoutDays
        .drop(maxOf(0, currentWorkoutIndex - 3))
        .take(7)
    val nextMilestone = listOf(3, 5, 7, 14, 21, 30).firstOrNull { currentStreak < it }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.dashboard_checkin_record),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.dashboard_day_streak_format, currentStreak),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔥", fontSize = 24.sp)
                }
            }

            Text(
                stringResource(R.string.dashboard_motivation_message),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.dashboard_checkin_recent),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentWorkoutDays.forEach { day ->
                        val isDone = day.dayNumber in completedDays
                        val isNext = day.dayNumber == currentWorkoutDay
                        CheckInDayChip(
                            dayNumber = day.dayNumber,
                            isDone = isDone,
                            isNext = isNext
                        )
                    }
                }
            }

            if (nextMilestone != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(
                            R.string.dashboard_checkin_next_milestone_format,
                            nextMilestone - currentStreak,
                            nextMilestone
                        ),
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Progress Bar with milestones
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val maxDays = 7
                val progress = (currentStreak.toFloat() / maxDays).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val milestones = listOf(2, 5, 7)
                    for (i in 1..maxDays) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(24.dp)
                        ) {
                            Text(
                                text = if (i in milestones) "🔥" else "",
                                fontSize = 12.sp,
                                modifier = Modifier.height(16.dp)
                            )
                            Text(
                                text = "$i",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (i <= currentStreak)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckInDayChip(
    dayNumber: Int,
    isDone: Boolean,
    isNext: Boolean
) {
    val backgroundColor = when {
        isDone -> MaterialTheme.colorScheme.primary
        isNext -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
    }
    val textColor = when {
        isDone -> MaterialTheme.colorScheme.onPrimary
        isNext -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayNumber.toString(),
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when {
                isDone -> stringResource(R.string.dashboard_checkin_done)
                isNext -> stringResource(R.string.dashboard_checkin_next)
                else -> stringResource(R.string.dashboard_checkin_pending)
            },
            color = textColor,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun quoteForDay(dayIndex: Int): String = when (dayIndex) {
    0 -> stringResource(R.string.dashboard_quote_sunday)
    1 -> stringResource(R.string.dashboard_quote_monday)
    2 -> stringResource(R.string.dashboard_quote_tuesday)
    3 -> stringResource(R.string.dashboard_quote_wednesday)
    4 -> stringResource(R.string.dashboard_quote_thursday)
    5 -> stringResource(R.string.dashboard_quote_friday)
    6 -> stringResource(R.string.dashboard_quote_saturday)
    else -> stringResource(R.string.dashboard_quote_default)
}

@Composable
fun WeeklyGoalSection(
    weeklyGoal: Int = 3,
    workoutPlan: List<DayPlan> = emptyList(),
    completedDays: Set<Int> = emptySet(),
    onEditGoal: () -> Unit = {},
    onToggleDay: (Int) -> Unit = {}
) {
    val workoutDays = workoutPlan.filter { !it.isRest }
    val activeDay = workoutDays.firstOrNull { it.dayNumber !in completedDays } ?: workoutDays.lastOrNull()
    val activeWeekIndex = ((activeDay?.dayNumber ?: 1) - 1) / 7
    val weekStartDay = (activeWeekIndex * 7) + 1
    val weekEndDay = minOf(weekStartDay + 6, workoutPlan.maxOfOrNull { it.dayNumber } ?: weekStartDay)
    val weekPlans = if (workoutPlan.isNotEmpty()) {
        workoutPlan.filter { it.dayNumber in weekStartDay..weekEndDay }
    } else {
        emptyList()
    }
    val weeklyWorkoutTarget = weeklyGoal.coerceAtLeast(1)

    val completedCount = weekPlans.count { !it.isRest && it.dayNumber in completedDays }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        stringResource(R.string.dashboard_weekly_goal),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(
                        onClick = onEditGoal,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.dashboard_edit_goal),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    stringResource(R.string.dashboard_weekly_goal_progress, completedCount, weeklyWorkoutTarget),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (completedCount >= weeklyWorkoutTarget)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.planner_week_format, activeWeekIndex + 1),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekPlans.forEach { dayPlan ->
                    val isCompleted = dayPlan.dayNumber in completedDays
                    val isCurrent = !dayPlan.isRest && dayPlan.dayNumber == activeDay?.dayNumber
                    val isClickable = !dayPlan.isRest && dayPlan.dayNumber <= (activeDay?.dayNumber ?: dayPlan.dayNumber)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    when {
                                        isCompleted -> MaterialTheme.colorScheme.primary
                                        isCurrent -> MaterialTheme.colorScheme.onBackground
                                        dayPlan.isRest -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                                        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                                    },
                                    CircleShape
                                )
                                .then(
                                    if (isClickable) Modifier.clickable { onToggleDay(dayPlan.dayNumber) }
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (dayPlan.isRest) "R" else dayPlan.dayNumber.toString(),
                                fontSize = 12.sp,
                                fontWeight = if (isCompleted || isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isCompleted || isCurrent -> MaterialTheme.colorScheme.background
                                    dayPlan.isRest -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                                }
                            )
                        }
                        Text(
                            text = when {
                                isCompleted -> stringResource(R.string.dashboard_checkin_done)
                                isCurrent -> stringResource(R.string.dashboard_checkin_next)
                                dayPlan.isRest -> stringResource(R.string.planner_status_rest)
                                else -> stringResource(R.string.dashboard_checkin_pending)
                            },
                            color = when {
                                isCompleted || isCurrent -> MaterialTheme.colorScheme.onBackground
                                dayPlan.isRest -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Coach quote dialog
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coach avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💪", fontSize = 22.sp)
                    }

                    // Quote
                    Text(
                        text = quoteForDay(LocalDate.now().dayOfWeek.value % 7),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutsSummarySection(
    completedCount: Int,
    totalWorkoutDays: Int,
    totalKcal: Int,
    onStartWorkout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.dashboard_workouts),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "$completedCount",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        stringResource(R.string.dashboard_days_suffix_format, totalWorkoutDays),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        stringResource(R.string.dashboard_completed),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "$totalKcal",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        stringResource(R.string.dashboard_kcal),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        stringResource(R.string.dashboard_burned),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onStartWorkout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        ) {
            Text(
                stringResource(R.string.dashboard_start_workout),
                color = MaterialTheme.colorScheme.background,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun HealthMetricsSection(
    metrics: DailyHealthMetrics,
    isActivityRecognitionGranted: Boolean,
    isStepSensorEnabled: Boolean,
    isStepTrackingActive: Boolean,
    onUnlockStepSensor: () -> Unit,
    onAddWater: (Int) -> Unit,
    onSetWaterGoal: (Int) -> Unit
) {
    var showGoalDialog by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf(metrics.waterGoalMl.toString()) }

    Text(
        stringResource(R.string.dashboard_health_metrics),
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        fontWeight = FontWeight.Bold,
        letterSpacing = 3.sp
    )
    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MetricHorizontalCard(
            modifier = Modifier.weight(1f),
            icon = Lucide.Footprints,
            iconTint = MaterialTheme.colorScheme.primary,
            value = metrics.steps.toString(),
            unit = stringResource(R.string.dashboard_steps).uppercase(),
            buttonText = when {
                !isActivityRecognitionGranted -> stringResource(R.string.dashboard_unlock)
                !isStepSensorEnabled -> stringResource(R.string.dashboard_manual_mode)
                isStepTrackingActive -> stringResource(R.string.dashboard_live_sensor)
                else -> stringResource(R.string.dashboard_sensor_ready)
            },
            onClick = if (!isActivityRecognitionGranted) onUnlockStepSensor else null
        )
        MetricHorizontalCard(
            modifier = Modifier.weight(1f),
            icon = Lucide.GlassWater,
            iconTint = MaterialTheme.colorScheme.secondary,
            value = metrics.waterIntakeMl.toString(),
            unit = stringResource(
                R.string.dashboard_water_with_goal_format,
                stringResource(R.string.dashboard_water).uppercase(),
                metrics.waterGoalMl
            ),
            buttonText = stringResource(R.string.dashboard_add_250_ml),
            onClick = { onAddWater(250) }
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = { showGoalDialog = true }) {
            Text(
                stringResource(R.string.dashboard_set_goal),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
            }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text(stringResource(R.string.dashboard_water_goal_title)) },
            text = {
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it.filter { c -> c.isDigit() } },
                    singleLine = true,
                    label = { Text(stringResource(R.string.dashboard_goal_label)) }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = goalInput.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            onSetWaterGoal(parsed)
                            showGoalDialog = false
                        }
                    }
                ) { Text(stringResource(R.string.common_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = when {
            !isActivityRecognitionGranted -> stringResource(R.string.dashboard_steps_permission_off)
            !isStepSensorEnabled -> stringResource(R.string.dashboard_steps_unavailable)
            !isStepTrackingActive -> stringResource(R.string.dashboard_steps_tracking_paused)
            else -> stringResource(R.string.dashboard_steps_live_active)
        },
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun MetricHorizontalCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    unit: String,
    buttonText: String,
    onClick: (() -> Unit)?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    unit,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    letterSpacing = 2.sp
                )
            }
            if (onClick != null) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.1f
                        )
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        buttonText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        buttonText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    FitflowTheme {
        DashboardScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun CheckInRecordSectionPreview() {
    FitflowTheme {
        CheckInRecordSection(
            workoutPlan = listOf(
                DayPlan(dayNumber = 1, isRest = false, workoutExercises = emptyList()),
                DayPlan(dayNumber = 2, isRest = false, workoutExercises = emptyList()),
                DayPlan(dayNumber = 3, isRest = false, workoutExercises = emptyList())
            ),
            completedDays = setOf(1, 2),
            currentStreak = 2
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeeklyCalendarSectionPreview() {
    FitflowTheme {
        WeeklyGoalSection()
    }
}