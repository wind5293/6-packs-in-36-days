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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.data.model.DayPlan
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
    onReCalibrate: () -> Unit = {}
) {
    val completedCount = completedDays.size
    val totalKcal = workoutPlan
        .filter { it.dayNumber in completedDays }
        .flatMap { it.exercises }
        .sumOf { it.kcal }
    val totalMinutes = workoutPlan
        .filter { it.dayNumber in completedDays }
        .flatMap { it.exercises }
        .sumOf { it.durationSec } / 60

    val today = LocalDate.now()
    val weekStart = today.with(DayOfWeek.MONDAY)
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    val weeklyMinutes = (0..6).map { offset ->
        if (startDate == null) 0
        else {
            val date = weekStart.plusDays(offset.toLong())
            val dayNum = ChronoUnit.DAYS.between(startDate, date).toInt() + 1
            if (dayNum in completedDays)
                workoutPlan.find { it.dayNumber == dayNum }?.exercises?.sumOf { it.durationSec }?.div(60) ?: 0
            else 0
        }
    }

    val weeklyKcal = (0..6).map { offset ->
        if (startDate == null) 0
        else {
            val date = weekStart.plusDays(offset.toLong())
            val dayNum = ChronoUnit.DAYS.between(startDate, date).toInt() + 1
            if (dayNum in completedDays)
                workoutPlan.find { it.dayNumber == dayNum }?.exercises?.sumOf { it.kcal } ?: 0
            else 0
        }
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
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "IDENTITY",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp
                )
                Text(
                    "PROFILE",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic
                )
            }
            IconButton(
                onClick = {},
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
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
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
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

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "OPERATIONS",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Re-calibrate card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .clickable { onReCalibrate() }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "RE-CALIBRATE BODY STATS",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp, fontWeight = FontWeight.Black
                        )
                        Text(
                            "REVIEW YOUR ONBOARDING SETUP",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp
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
            fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp
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
