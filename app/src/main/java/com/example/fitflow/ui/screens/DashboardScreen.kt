package com.example.fitflow.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.composables.icons.lucide.Footprints
import com.composables.icons.lucide.GlassWater
import com.composables.icons.lucide.Lucide
import com.example.fitflow.R
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.DailyHealthMetrics
import com.example.fitflow.data.model.StepSource
import com.example.fitflow.domain.PushYourLimitsCatalog
import com.example.fitflow.ui.components.PushYourLimitsSection
import com.example.fitflow.ui.theme.FitflowTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    completedDays: Set<Int> = emptySet(),
    completedDateMap: Map<LocalDate, Int> = emptyMap(),
    currentStreak: Int = 0,
    workoutPlan: List<DayPlan> = emptyList(),
    userProfile: UserProfile? = null,
    startDate: LocalDate? = null,
    healthMetrics: DailyHealthMetrics = DailyHealthMetrics(LocalDate.now(), 0, 0, 2000, StepSource.MANUAL),
    isActivityRecognitionGranted: Boolean = false,
    isStepSensorEnabled: Boolean = false,
    isStepTrackingActive: Boolean = false,
    onUnlockStepSensor: () -> Unit = {},
    onAddWater: (Int) -> Unit = {},
    onSetWaterGoal: (Int) -> Unit = {},
    onStartWorkout: () -> Unit = {},
    onOpenChatbot: () -> Unit = {},
    onOpenPlanner: () -> Unit = {},
    onOpenSupplementary: (String) -> Unit = {},
    onOpenDaySummary: (Int, Long) -> Unit = { _, _ -> },
    onOpenHistory: () -> Unit = {}
) {
    val totalWorkoutDays = workoutPlan.count { !it.isRest }
    val completedCount = completedDays.size
    val totalKcal = workoutPlan
        .filter { it.dayNumber in completedDays }
        .flatMap { it.workoutExercises }
        .sumOf { it.kcal }
    val currentDayPlan = workoutPlan
        .filter { !it.isRest }
        .firstOrNull { it.dayNumber !in completedDays }

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
            HeaderSection(onOpenChatbot = onOpenChatbot)
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
        item {
            WeeklyGoalSection(
                completedDays = completedDays,
                startDate = startDate,
                completedDateMap = completedDateMap,
                onViewHistory = onOpenHistory,
                onToggleDay = { weekIndex ->
                    val today = LocalDate.now()
                    val todayIndex = if (today.dayOfWeek.value == 7) 0 else today.dayOfWeek.value
                    val startOfWeek = today.minusDays(todayIndex.toLong())
                    val date = startOfWeek.plusDays(weekIndex.toLong())
                    
                    val dayNum = completedDateMap[date] ?: -1
                    onOpenDaySummary(dayNum, date.toEpochDay())
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            if (currentDayPlan != null) {
                DailyChallengeSection(
                    currentDay = currentDayPlan.dayNumber,
                    totalDays = workoutPlan.count { !it.isRest },
                    dayTitle = currentDayPlan.title,
                    exercises = currentDayPlan.workoutExercises.map { it.name },
                    onClick = onOpenPlanner
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(18.dp))
        }
        item {
            // Push Your Limits horizontal scroller — uses local catalog
            PushYourLimitsSection(
                workouts = PushYourLimitsCatalog.all(),
                onWorkoutClick = onOpenSupplementary,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
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
fun HeaderSection(
    onOpenChatbot: () -> Unit = {}
) {
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
            onClick = onOpenChatbot,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    RoundedCornerShape(50)
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    RoundedCornerShape(50)
                )
        ) {
            Icon(
                Icons.Default.Chat,
                contentDescription = "AI Coach",
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
        }
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
    completedDays: Set<Int> = emptySet(),
    completedDateMap: Map<LocalDate, Int> = emptyMap(),
    startDate: LocalDate? = null,
    onViewHistory: () -> Unit = {},
    onEditGoal: () -> Unit = {},
    onToggleDay: (Int) -> Unit = {}
) {
    val today = LocalDate.now()
    val todayIndex = if (today.dayOfWeek.value == 7) 0 else today.dayOfWeek.value
    val startOfWeek = today.minusDays(todayIndex.toLong())

    val completedCount = completedDays.size

    var todayOffsetX by remember { mutableStateOf(0f) }
    var rowWidth by remember { mutableStateOf(0f) }

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
                        onClick = onViewHistory,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "View History",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    stringResource(R.string.dashboard_weekly_goal_progress, completedCount, weeklyGoal),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (completedCount >= weeklyGoal)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Days row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { cords ->
                        rowWidth = cords.size.width.toFloat()
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0..6) {
                    val date = startOfWeek.plusDays(i.toLong())
                    val isToday = i == todayIndex
                    val isPast = i < todayIndex

                    val isCompleted = date in completedDateMap

                    val bgColor = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.onBackground
                        else -> Color.Transparent
                    }
                    val textColor = when {
                        isCompleted || isToday -> MaterialTheme.colorScheme.background
                        isPast -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = if (isToday) {
                            Modifier.onGloballyPositioned { coords ->
                                // tâm của circle ngày hôm nay
                                todayOffsetX = coords.positionInParent().x + coords.size.width / 2f
                            }
                        } else Modifier
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(bgColor, CircleShape)
                                .clickable { onToggleDay(i) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${date.dayOfMonth}",
                                fontSize = 14.sp,
                                fontWeight = if (isToday || isCompleted) FontWeight.Bold else FontWeight.Normal,
                                color = textColor
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val arrowColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)
            if (rowWidth > 0f && todayOffsetX > 0f) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                ) {
                    val arrowW = 16.dp.toPx()
                    val arrowH = 8.dp.toPx()
                    val tipX = todayOffsetX.coerceIn(arrowW / 2f, size.width - arrowW / 2f)
                    val path = Path().apply {
                        moveTo(tipX, 0f)
                        lineTo(tipX + arrowW / 2f, arrowH)
                        lineTo(tipX - arrowW / 2f, arrowH)
                        close()
                    }
                    drawPath(path = path, color = arrowColor)
                }
            }

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
                        text = quoteForDay(todayIndex),
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
fun DailyChallengeSection(
    currentDay: Int,
    totalDays: Int,
    dayTitle: String,
    exercises: List<String>,
    onClick: () -> Unit
) {
    val completedDays = currentDay - 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Daily Challenge",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = { /* Hành động bộ lọc tinh chỉnh */ },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filter",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF6D00),
                            Color(0xFFFF3D00)
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 8.dp)
                    .size(width = 140.dp, height = 150.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.co_bung_2),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(
                            topStart = 32.dp,
                            bottomStart = 32.dp,
                            topEnd = 16.dp,
                            bottomEnd = 16.dp
                        ))
                        .alpha(0.9f)
                )
            }

            // ── Foreground content ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Program name + difficulty
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        dayTitle.uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // Day number — big
                Text(
                    "Day $currentDay",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 48.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Progress counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "$completedDays",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "/$totalDays Days",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                // START button
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        "START",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
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

@Preview(showBackground = true, name = "Daily Challenge - Light Mode")
@Composable
fun DailyChallengeSectionPreview() {
    // Ép buộc preview sử dụng hệ màu nền sáng (Light Color Scheme) để đồng bộ với app mẫu
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = Color(0xFFF8F9FA), // Nền trắng xám nhạt cao cấp của app mẫu
            onBackground = Color(0xFF1A1A1A), // Chữ màu đen xám đậm dễ đọc
            primary = Color(0xFFFF5722), // Màu cam thương hiệu
            onPrimary = Color.White
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                DailyChallengeSection(
                    currentDay = 1,
                    totalDays = 30,
                    dayTitle = "Rock Hard Abs",
                    exercises = listOf("Crunch", "Plank", "Leg Raise"),
                    onClick = { /* Không xử lý hành động trong preview */ }
                )
            }
        }
    }
}