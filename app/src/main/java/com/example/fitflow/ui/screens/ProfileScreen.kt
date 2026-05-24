package com.example.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.DailyHealthMetrics
import com.example.fitflow.data.model.UserProfile
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun ProfileScreen(
    userProfile: UserProfile? = null,
    completedDays: Set<Int> = emptySet(),
    workoutPlan: List<DayPlan> = emptyList(),
    startDate: LocalDate? = null,
    weightHistory: List<Pair<LocalDate, Float>> = emptyList(),
    healthMetricsHistory: List<DailyHealthMetrics> = emptyList(),
    onRecordWeight: (Float) -> Unit = {},
    onReCalibrate: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val completedCount = completedDays.size
    val totalKcal = workoutPlan
        .filter { it.dayNumber in completedDays }
        .flatMap { it.workoutExercises }
        .sumOf { it.kcal }
    val totalMinutes = workoutPlan
        .filter { it.dayNumber in completedDays }
        .flatMap { it.workoutExercises }
        .sumOf { it.durationSec } / 60

    val today = LocalDate.now()
    val weekStart = today.with(DayOfWeek.MONDAY)
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    var recordInput by remember { mutableStateOf(userProfile?.weight?.toString() ?: "") }
    var inputError by remember { mutableStateOf<String?>(null) }

    val weeklyMinutes = (0..6).map { offset ->
        if (startDate == null) 0
        else {
            val date = weekStart.plusDays(offset.toLong())
            val dayNum = ChronoUnit.DAYS.between(startDate, date).toInt() + 1
            if (dayNum in completedDays)
                workoutPlan.find { it.dayNumber == dayNum }?.workoutExercises?.sumOf { it.durationSec }?.div(60) ?: 0
            else 0
        }
    }

    val weeklyKcal = (0..6).map { offset ->
        if (startDate == null) 0
        else {
            val date = weekStart.plusDays(offset.toLong())
            val dayNum = ChronoUnit.DAYS.between(startDate, date).toInt() + 1
            if (dayNum in completedDays)
                workoutPlan.find { it.dayNumber == dayNum }?.workoutExercises?.sumOf { it.kcal } ?: 0
            else 0
        }
    }

    val healthByDay = healthMetricsHistory.associateBy { it.date }
    val weeklySteps = (0..6).map { offset ->
        val date = weekStart.plusDays(offset.toLong())
        healthByDay[date]?.steps ?: 0
    }
    val weeklyWaterPct = (0..6).map { offset ->
        val date = weekStart.plusDays(offset.toLong())
        val metric = healthByDay[date] ?: return@map 0
        if (metric.waterGoalMl <= 0) 0 else ((metric.waterIntakeMl * 100f / metric.waterGoalMl).toInt()).coerceIn(0, 100)
    }

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
            Column {
                Text(
                    "IDENTITY",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Row {
                    Text(
                        "PROFILE",
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
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
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
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Welcome, my friend!",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActivityStatItem("WORKOUTS", completedCount.toString(), "days")
                    ActivityStatItem("KCAL", totalKcal.toString(), "kcal")
                    ActivityStatItem("MINUTES", totalMinutes.toString(), "min")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Body stats card (Weight / Height / BMI only)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                    RoundedCornerShape(32.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatsItem("WEIGHT", userProfile?.weight?.let { "%.1f".format(it) } ?: "-", "kg", false)
                StatsItem("HEIGHT", userProfile?.height?.let { "%.0f".format(it) } ?: "-", "cm", false)
                StatsItem("BMI", userProfile?.bmi?.let { "%.1f".format(it) } ?: "-", "", true)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekly charts row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeeklyChartCard(
                title = "DURATION",
                totalValue = totalMinutes.toString(),
                unit = "min",
                color = MaterialTheme.colorScheme.primary,
                values = weeklyMinutes,
                labels = dayLabels,
                modifier = Modifier.weight(1f)
            )
            WeeklyChartCard(
                title = "CALORIES",
                totalValue = totalKcal.toString(),
                unit = "kcal",
                color = MaterialTheme.colorScheme.secondary,
                values = weeklyKcal,
                labels = dayLabels,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeeklyChartCard(
                title = "STEPS",
                totalValue = weeklySteps.sum().toString(),
                unit = "steps",
                color = MaterialTheme.colorScheme.primary,
                values = weeklySteps,
                labels = dayLabels,
                modifier = Modifier.weight(1f)
            )
            WeeklyChartCard(
                title = "WATER",
                totalValue = "${weeklyWaterPct.average().toInt()}%",
                unit = "goal",
                color = MaterialTheme.colorScheme.secondary,
                values = weeklyWaterPct,
                labels = dayLabels,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        WeightTrackingSection(
            currentWeight = userProfile?.weight,
            targetWeight = userProfile?.targetWeight,
            weightHistory = weightHistory,
            recordInput = recordInput,
            inputError = inputError,
            onRecordInputChange = {
                recordInput = it
                inputError = null
            },
            onRecord = {
                val parsed = recordInput.toFloatOrNull()
                if (parsed == null || parsed <= 0f) {
                    inputError = "Please enter a valid weight"
                } else {
                    onRecordWeight(parsed)
                    recordInput = String.format("%.1f", parsed)
                    inputError = null
                }
            }
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
                containerColor = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = 0.05f
                )
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
            fontSize = 28.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic
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
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    totalValue,
                    color = color,
                    fontSize = 24.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    unit,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            WeeklyBarChart(values = values, labels = labels, color = color)
        }
    }
}

@Composable
fun WeeklyBarChart(values: List<Int>, labels: List<String>, color: Color) {
    val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        labels.zip(values).forEach { (label, value) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height((48f * value / maxValue).dp.coerceAtLeast(3.dp))
                        .background(
                            if (value > 0) color else color.copy(alpha = 0.1f),
                            RoundedCornerShape(3.dp)
                        )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    label,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold
                )
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
                fontStyle = FontStyle.Italic
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
    recordInput: String,
    inputError: String?,
    onRecordInputChange: (String) -> Unit,
    onRecord: () -> Unit
) {
    val latest = weightHistory.lastOrNull()?.second ?: currentWeight
    val progressText = if (latest != null && targetWeight != null) {
        val diff = latest - targetWeight
        "${String.format("%.1f", diff)} kg to goal"
    } else {
        "No target set"
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "WEIGHT TRACKING",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        latest?.let { "${String.format("%.1f", it)} kg" } ?: "-",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        progressText,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
                Text(
                    "RECORD",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            WeightHistoryMiniChart(
                history = weightHistory,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = recordInput,
                    onValueChange = onRecordInputChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Current weight") },
                    isError = inputError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Button(
                    onClick = onRecord,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("SAVE", fontWeight = FontWeight.Black)
                }
            }

            if (inputError != null) {
                Text(
                    inputError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp
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
    val points = history.takeLast(7)
    val maxValue = points.maxOfOrNull { it.second } ?: 1f
    val minValue = points.minOfOrNull { it.second } ?: 0f
    val range = (maxValue - minValue).coerceAtLeast(0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        if (points.isEmpty()) {
            Text(
                "No weight records yet",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 12.sp
            )
        } else {
            points.forEach { (date, value) ->
                val ratio = ((value - minValue) / range).coerceIn(0f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height((22.dp + 54.dp * ratio))
                            .background(color.copy(alpha = 0.75f), RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${date.dayOfMonth}",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
