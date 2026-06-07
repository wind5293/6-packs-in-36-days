package com.example.fitflow.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowBack
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.HeartPulse
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.DailyHealthMetrics
import com.example.fitflow.data.model.UserProfile
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    userProfile: UserProfile? = null,
    completedDays: Set<Int> = emptySet(),
    workoutPlan: List<DayPlan> = emptyList(),
    startDate: LocalDate? = null,
    completedDateMap: Map<LocalDate, Int> = emptyMap(),
    weightHistory: List<Pair<LocalDate, Float>> = emptyList(),
    healthMetricsHistory: List<DailyHealthMetrics> = emptyList(),
    todayHealthMetrics: DailyHealthMetrics? = null,
    globalWorkoutLogs: List<com.example.fitflow.data.model.WorkoutLogEntry> = emptyList(),
    isActivityRecognitionGranted: Boolean = false,
    isStepSensorEnabled: Boolean = false,
    isStepTrackingActive: Boolean = false,
    onRecordWeight: (Float, LocalDate) -> Unit = { _, _ -> },
    onReCalibrate: () -> Unit = {},
    onUnlockStepSensor: () -> Unit = {},
    onAddWater: (Int) -> Unit = {},
    onSetWaterGoal: (Int) -> Unit = {},
    onSetStepGoal: (Int) -> Unit = {},
    onDemoNotification: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val completedCount = completedDays.size
    val planByDayNumber = remember(workoutPlan) { workoutPlan.associateBy { it.dayNumber } }
    val totalKcal = remember(completedDays, workoutPlan) {
        workoutPlan
            .filter { it.dayNumber in completedDays }
            .flatMap { it.workoutExercises }
            .sumOf { it.kcal }
    }
    val totalMinutes = remember(completedDays, workoutPlan) {
        workoutPlan
            .filter { it.dayNumber in completedDays }
            .flatMap { it.workoutExercises }
            .sumOf { ex -> if (ex.durationSec > 0) ex.durationSec else ex.reps * 3 } / 60
    }

    val today = LocalDate.now()
    val weekStart = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
    val todayLabelIndex = dayLabels.indices.firstOrNull { idx ->
        weekStart.plusDays(idx.toLong()) == today
    } ?: -1
    val latestRecordedWeight = weightHistory.lastOrNull()?.second ?: userProfile?.weight ?: 70f
    var currentWeightKg by remember(latestRecordedWeight) { mutableStateOf(latestRecordedWeight) }
    var showWeightSheet by remember { mutableStateOf(false) }

    val weeklyDayDates = remember(weekStart) {
        (0..6).map { offset -> weekStart.plusDays(offset.toLong()) }
    }

    val logsByDate = remember(globalWorkoutLogs) {
        globalWorkoutLogs.groupBy { LocalDate.ofEpochDay(it.dateEpochDay) }
    }

    val weeklyDurationSeconds = remember(weeklyDayDates, logsByDate) {
        weeklyDayDates.map { date ->
            logsByDate[date]?.sumOf { it.durationSec } ?: 0
        }
    }

    val weeklyMinutes = remember(weeklyDurationSeconds) {
        weeklyDurationSeconds.map { seconds -> (seconds / 60f).roundToInt() }
    }

    val weeklyKcal = remember(weeklyDayDates, logsByDate) {
        weeklyDayDates.map { date ->
            (logsByDate[date]?.sumOf { it.kcal } ?: 0) / 10
        }
    }

    val weeklyTotalMinutes = remember(weeklyDurationSeconds) {
        (weeklyDurationSeconds.sum() / 60f).roundToInt()
    }
    val weeklyTotalKcal = remember(weeklyKcal) { weeklyKcal.sum() }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Column {
                    Row {
                        Text(
                            "REPORT",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Normal
                        )
                    }
                }
            }
        }

        // Welcome card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Lucide.HeartPulse,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatsItem("WEIGHT", userProfile?.weight?.let { "%.1f".format(it) } ?: "-", "kg", false)
                    StatsItem("HEIGHT", userProfile?.height?.let { "%.0f".format(it) } ?: "-", "cm", false)
                    StatsItem("BMI", userProfile?.bmi?.let { "%.1f".format(it) } ?: "-", "", true)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (todayHealthMetrics != null) {
            HealthMetricsSection(
                metrics = todayHealthMetrics,
                isActivityRecognitionGranted = isActivityRecognitionGranted,
                isStepSensorEnabled = isStepSensorEnabled,
                isStepTrackingActive = isStepTrackingActive,
                onUnlockStepSensor = onUnlockStepSensor,
                onAddWater = onAddWater,
                onSetWaterGoal = onSetWaterGoal,
                onSetStepGoal = onSetStepGoal
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Weekly charts (responsive): keep 2-up layout on normal width, stack on compact width.
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompact = maxWidth < 360.dp
            if (isCompact) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    WeeklyChartCard(
                        title = "Duration",
                        totalValue = weeklyTotalMinutes.toString(),
                        unit = "min",
                        color = Color(0xFF3B66FF),
                        values = weeklyMinutes,
                        labels = dayLabels,
                        highlightedLabelIndex = todayLabelIndex,
                        modifier = Modifier.fillMaxWidth()
                    )
                    WeeklyChartCard(
                        title = "Calories",
                        totalValue = weeklyTotalKcal.toString(),
                        unit = "kcal",
                        color = Color(0xFFFF6A00),
                        values = weeklyKcal,
                        labels = dayLabels,
                        highlightedLabelIndex = todayLabelIndex,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WeeklyChartCard(
                        title = "Duration",
                        totalValue = weeklyTotalMinutes.toString(),
                        unit = "min",
                        color = Color(0xFF3B66FF),
                        values = weeklyMinutes,
                        labels = dayLabels,
                        highlightedLabelIndex = todayLabelIndex,
                        modifier = Modifier.weight(1f)
                    )
                    WeeklyChartCard(
                        title = "Calories",
                        totalValue = weeklyTotalKcal.toString(),
                        unit = "kcal",
                        color = Color(0xFFFF6A00),
                        values = weeklyKcal,
                        labels = dayLabels,
                        highlightedLabelIndex = todayLabelIndex,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        WeightTrackingSection(
            currentWeight = currentWeightKg,
            targetWeight = userProfile?.targetWeight,
            weightHistory = weightHistory,
            onUpdateWeightClick = { showWeightSheet = true }
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "OPERATIONS",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Re-calibrate card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                    RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .clickable { onReCalibrate() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "RE-CALIBRATE BODY STATS",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "REVIEW YOUR ONBOARDING SETUP",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                    }
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Demo Notification card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                    RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .clickable { onDemoNotification() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "DEMO NOTIFICATION",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "SEND A TEST REMINDER IN 5S",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                    }
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showWeightSheet) {
        WeightPickerSheet(
            initialWeight = currentWeightKg,
            onDismiss = { showWeightSheet = false },
            onSave = { newWeight, date ->
                currentWeightKg = newWeight
                onRecordWeight(newWeight, date)
                showWeightSheet = false
            }
        )
    }
}

@Composable
fun ActivityStatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 28.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Normal
        )
        Text(
            unit,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 11.sp
        )
    }
}

@Composable
fun WeeklyChartCard(
    title: String,
    totalValue: String,
    unit: String,
    color: Color,
    values: List<Int>,
    labels: List<String>,
    highlightedLabelIndex: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.border(
            1.dp,
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
            RoundedCornerShape(24.dp)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    totalValue,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Normal
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    unit,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            WeeklyBarChart(
                values = values,
                labels = labels,
                color = color,
                highlightedLabelIndex = highlightedLabelIndex
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun WeeklyBarChart(
    values: List<Int>,
    labels: List<String>,
    color: Color,
    highlightedLabelIndex: Int
) {
    val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    Box(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            labels.zip(values).forEachIndexed { index, (label, value) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 1.dp)
                        .height((52f * value / maxValue).dp.coerceAtLeast(6.dp))
                        .background(
                            if (value > 0) color else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                            RoundedCornerShape(6.dp)
                        )
                )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        label,
                        fontSize = 10.sp,
                        color = if (index == highlightedLabelIndex) color else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StatsItem(label: String, value: String, unit: String, isHighlight: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Normal
            )
            if (unit.isNotEmpty()) {
                Text(
                    unit,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                )
            }
        }
    }
}

@Composable
fun WeightTrackingSection(
    currentWeight: Float?,
    targetWeight: Float?,
    weightHistory: List<Pair<LocalDate, Float>>,
    onUpdateWeightClick: () -> Unit
) {
    val latest = weightHistory.lastOrNull()?.second ?: currentWeight
    val progressText = if (latest != null && targetWeight != null) {
        val diff = latest - targetWeight
        "${String.format("%.1f", diff)} kg to goal"
    } else {
        "No target set"
    }
    val todayLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().border(
            1.dp,
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
            RoundedCornerShape(32.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Weight",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    todayLabel,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Text(
                latest?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "-",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                progressText,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            WeightHistoryMiniChart(
                history = weightHistory,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = onUpdateWeightClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    "UPDATE WEIGHT",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimary,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun WeightHistoryMiniChart(
    history: List<Pair<LocalDate, Float>>,
    color: Color
) {
    val points = history
    
    if (points.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No weight records yet",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 12.sp
            )
        }
        return
    }

    val maxValue = points.maxOfOrNull { it.second } ?: 1f
    val minValue = points.minOfOrNull { it.second } ?: 0f
    
    val range = (maxValue - minValue).coerceAtLeast(1f)
    val yMax = maxValue + (range * 0.1f)
    val yMin = (minValue - (range * 0.1f)).coerceAtLeast(0f)
    val yRange = (yMax - yMin).coerceAtLeast(1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 24.dp, end = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Text(String.format("%.1f", yMax), fontSize = 10.sp, color = Color(0xFFAEB5C4), fontWeight = FontWeight.Medium)
            Text(String.format("%.1f", (yMax + yMin) / 2), fontSize = 10.sp, color = Color(0xFFAEB5C4), fontWeight = FontWeight.Medium)
            Text(String.format("%.1f", yMin), fontSize = 10.sp, color = Color(0xFFAEB5C4), fontWeight = FontWeight.Medium)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(3) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), thickness = 1.dp)
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp, start = 4.dp, end = 4.dp)
            ) {
                val width = size.width
                val height = size.height
                val stepX = if (points.size > 1) width / (points.size - 1) else width / 2
                val startX = if (points.size == 1) width / 2 else 0f

                val coordinates = points.mapIndexed { index, pair ->
                    val value = pair.second
                    val ratio = ((value - yMin) / yRange).coerceIn(0f, 1f)
                    val x = startX + index * stepX
                    val y = height - (ratio * height)
                    Offset(x, y)
                }

                if (coordinates.size > 1) {
                    val path = Path().apply {
                        moveTo(coordinates.first().x, coordinates.first().y)
                        for (i in 1 until coordinates.size) {
                            lineTo(coordinates[i].x, coordinates[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.7f),
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                coordinates.forEach { offset ->
                    drawCircle(
                        color = color,
                        radius = 4.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = Color(0xFF171A21),
                        radius = 2.dp.toPx(),
                        center = offset
                    )
                }

                val paint = android.graphics.Paint().apply {
                    this.color = android.graphics.Color.parseColor("#AEB5C4")
                    this.textSize = 10.sp.toPx()
                    this.textAlign = android.graphics.Paint.Align.CENTER
                    this.isAntiAlias = true
                }

                val labelIndices = if (points.size <= 5) {
                    points.indices.toList()
                } else {
                    listOf(0, points.size / 2, points.lastIndex)
                }

                labelIndices.forEach { index ->
                    val x = startX + index * stepX
                    val date = points[index].first
                    val text = "${date.dayOfMonth}/${date.monthValue}"
                    drawContext.canvas.nativeCanvas.drawText(
                        text,
                        x,
                        height + 18.dp.toPx(),
                        paint
                    )
                }
            }
        }
    }
}
