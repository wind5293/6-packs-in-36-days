package com.example.fitflow.ui.screens

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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Footprints
import com.composables.icons.lucide.GlassWater
import com.composables.icons.lucide.Lucide
import com.example.fitflow.R
import com.example.fitflow.data.model.DayPlan
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    completedDays: Set<Int> = emptySet(),
    workoutPlan: List<DayPlan> = emptyList(),
    userProfile: UserProfile? = null,
    onStartWorkout: () -> Unit = {}
) {
    var steps by remember { mutableIntStateOf(0) }
    var water by remember { mutableIntStateOf(0) }

    val totalWorkoutDays = workoutPlan.count { !it.isRest }
    val completedCount = completedDays.size
    val totalKcal = workoutPlan
        .filter { it.dayNumber in completedDays }
        .flatMap { it.exercises }
        .sumOf { it.kcal }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {
            HeaderSection()
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
        item {
            if (userProfile != null) {
                TodaysWeightSection(userProfile = userProfile)
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            StreakSummarySection()
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            WeeklyCalendarSection()
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
                steps = steps,
                onAddSteps = { steps += 500 },
                water = water,
                onAddWater = { water += 250 }
            )
        }
    }
}

@Composable
fun TodaysWeightSection(userProfile: UserProfile) {
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
fun HeaderSection() {
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
            onClick = {},
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
                Icons.Default.Notifications,
                contentDescription = "Notify",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun StreakSummarySection() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .weight(1f)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    RoundedCornerShape(32.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Column {
                    Text(
                        stringResource(R.string.dashboard_0),
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        stringResource(R.string.dashboard_streak_cycle),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .weight(1f)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    RoundedCornerShape(32.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                )
                Column {
                    Text(
                        stringResource(R.string.dashboard_weight_loss),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        stringResource(R.string.dashboard_phase_01),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyCalendarSection() {
    val today = LocalDate.now()
    var weekOffset by remember { mutableIntStateOf(0) }
    // Sunday = 0, Monday = 1, ..., Saturday = 6
    val dayIndex = if (today.dayOfWeek.value == 7) 0 else today.dayOfWeek.value
    val startOfWeek = today.minusDays(dayIndex.toLong()).plusWeeks(weekOffset.toLong())
    val dayLetters = listOf("S", "M", "T", "W", "T", "F", "S")
    val monthYear = startOfWeek.format(DateTimeFormatter.ofPattern("MMMM yyyy")).uppercase()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { weekOffset-- }) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Previous week",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
            Text(
                monthYear,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            IconButton(onClick = { weekOffset++ }) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next week",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            for (i in 0..6) {
                val date = startOfWeek.plusDays(i.toLong())
                val isToday = date == today
                val isPast = date.isBefore(today)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        dayLetters[i],
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${date.dayOfMonth}",
                            color = when {
                                isToday -> MaterialTheme.colorScheme.onPrimary
                                isPast  -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                else    -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            },
                            fontSize = 13.sp,
                            fontWeight = if (isToday) FontWeight.Black else FontWeight.Normal
                        )
                    }
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
    steps: Int, onAddSteps: () -> Unit,
    water: Int, onAddWater: () -> Unit
) {
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
            value = "0",
            unit = stringResource(R.string.dashboard_steps).uppercase(),
            buttonText = "SET A GOAL",
            onClick = onAddSteps
        )
        MetricHorizontalCard(
            modifier = Modifier.weight(1f),
            icon = Lucide.GlassWater,
            iconTint = MaterialTheme.colorScheme.secondary,
            value = "0",
            unit = stringResource(R.string.dashboard_water).uppercase(),
            buttonText = "UNLOCK",
            onClick = onAddWater
        )
    }
}

@Composable
fun MetricHorizontalCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    unit: String,
    buttonText: String,
    onClick: () -> Unit
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
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
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
        }
    }
}
