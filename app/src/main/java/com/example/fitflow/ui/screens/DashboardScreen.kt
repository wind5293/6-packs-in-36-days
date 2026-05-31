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
import androidx.compose.ui.tooling.preview.Preview
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
    workoutPlan: List<DayPlan> = emptyList(),
    userProfile: UserProfile? = null,
    healthMetrics: DailyHealthMetrics = DailyHealthMetrics(LocalDate.now(), 0, 0, 2000, StepSource.MANUAL),
    isActivityRecognitionGranted: Boolean = false,
    isStepSensorEnabled: Boolean = false,
    onUnlockStepSensor: () -> Unit = {},
    onAddWater: (Int) -> Unit = {},
    onSetWaterGoal: (Int) -> Unit = {},
    onStartWorkout: () -> Unit = {},
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
            WeeklyGoalSection()
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
            CheckInRecordSection(completedCount = completedCount)
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
                    "TODAY'S WEIGHT",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${String.format("%.1f", userProfile.weight)} kg",
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
                    "${String.format("%.1f", weightLeft)} kg to goal",
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
                    "Updated Today!",
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
fun HeaderSection(onOpenSettings: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "STATUS REPORT",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Row {
                Text(
                    "DASHBOARD",
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
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun CheckInRecordSection(completedCount: Int) {
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
                        text = "CHECK-IN RECORD",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completedCount DAY STREAK",
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
                "Make today 1% better than yesterday!",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            // Progress Bar with milestones
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val maxDays = 7
                val progress = (completedCount.toFloat() / maxDays).coerceIn(0f, 1f)

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
                                color = if (i <= completedCount)
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
fun WeeklyGoalSection(
    weeklyGoal: Int = 3,
    completedDays: Set<Int> = emptySet(),
    onEditGoal: () -> Unit = {},
    onToggleDay: (Int) -> Unit = {}
) {
    val today = LocalDate.now()
    val todayIndex = if (today.dayOfWeek.value == 7) 0 else today.dayOfWeek.value
    val startOfWeek = today.minusDays(todayIndex.toLong())

    val completedCount = completedDays.size

    val onBackground = MaterialTheme.colorScheme.onBackground

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
                        "Weekly Goal",
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
                            contentDescription = "Edit goal",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    "$completedCount/$weeklyGoal workouts",
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0..6) {
                    val date = startOfWeek.plusDays(i.toLong())
                    val isToday = i == todayIndex
                    val isPast = i < todayIndex
                    val isCompleted = i in completedDays
                    val isClickable = isPast || isToday

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

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(bgColor, CircleShape)
                            .then(
                                if (isClickable) Modifier.clickable { onToggleDay(i) }
                                else Modifier
                            ),
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

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Canvas(
                    modifier = Modifier.size(width = 16.dp, height = 8.dp)
                ) {
                    val path = Path().apply {
                        moveTo(size.width / 2f, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = onBackground.copy(alpha = 0.04f)
                    )
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
                        text = "Your fitness goals are calling – time to answer with a workout!",
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
            "WORKOUTS",
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
                        "/ $totalWorkoutDays DAYS",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "COMPLETED",
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
                        "KCAL",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "BURNED",
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
                "START A WORKOUT",
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
                !isActivityRecognitionGranted -> "UNLOCK"
                metrics.stepSource == StepSource.SENSOR -> "LIVE SENSOR"
                else -> "MANUAL MODE"
            },
            onClick = if (!isActivityRecognitionGranted) onUnlockStepSensor else null
        )
        MetricHorizontalCard(
            modifier = Modifier.weight(1f),
            icon = Lucide.GlassWater,
            iconTint = MaterialTheme.colorScheme.secondary,
            value = metrics.waterIntakeMl.toString(),
            unit = "${stringResource(R.string.dashboard_water).uppercase()} / ${metrics.waterGoalMl} ML",
            buttonText = "+250 ML",
            onClick = { onAddWater(250) }
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = { onAddWater(500) },
            label = { Text("+500 ML") }
        )
        AssistChip(
            onClick = {
                goalInput = metrics.waterGoalMl.toString()
                showGoalDialog = true
            },
            label = { Text("SET GOAL") }
        )
    }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Water Goal (ml)") },
            text = {
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it.filter { c -> c.isDigit() } },
                    singleLine = true,
                    label = { Text("Goal") }
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
                ) { Text("SAVE") }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text("CANCEL") }
            }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = when {
            !isActivityRecognitionGranted -> "Step sensor permission is off. Using manual mode."
            !isStepSensorEnabled -> "Step sensor unavailable on this device. Manual mode enabled."
            else -> "Live step tracking is active."
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
        CheckInRecordSection(completedCount = 2)
    }
}

@Preview(showBackground = true)
@Composable
fun WeeklyCalendarSectionPreview() {
    FitflowTheme {
        WeeklyGoalSection()
    }
}