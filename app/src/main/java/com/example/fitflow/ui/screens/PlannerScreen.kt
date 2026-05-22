package com.example.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.ui.theme.FitflowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    workoutPlan: List<DayPlan>,
    completedDays: Set<Int> = emptySet(),
    currentDay: Int = -1,
    onDayClick: (Int) -> Unit = {}
) {
    val groupedByWeek = workoutPlan.groupBy { (it.dayNumber - 1) / 7 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "MASTER MANIFEST",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Row {
                    Text(
                        "PLANNER",
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groupedByWeek.forEach { (weekIndex, daysInWeek) ->
                val weekNum = weekIndex + 1
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "WEEK ${weekNum.toString().padStart(2, '0')}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontStyle = FontStyle.Italic
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .height(1.dp)
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                        )
                    }
                }
                items(daysInWeek) { dayPlan ->
                    DayPlanItem(
                        dayPlan = dayPlan,
                        isCurrentDay = dayPlan.dayNumber == currentDay,
                        isCompleted = dayPlan.dayNumber in completedDays,
                        onClick = { onDayClick(dayPlan.dayNumber) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {}
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "NEW CYCLE",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic
                            )
                            Text(
                                "RE-GENERATE ENTIRE LOGIC",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DayPlanItem(
    dayPlan: DayPlan,
    isCurrentDay: Boolean = false,
    isCompleted: Boolean = false,
    onClick: () -> Unit = {}
) {
    val dayNum = dayPlan.dayNumber
    val isRest = dayPlan.isRest
    val cardAlpha = if (isCompleted) 0.4f else 1f

    val borderColor = when {
        isCurrentDay -> MaterialTheme.colorScheme.primary
        else         -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
    }
    val cardBg = when {
        isCurrentDay -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        else         -> MaterialTheme.colorScheme.surface
    }
    val badgeBg = when {
        isCurrentDay -> MaterialTheme.colorScheme.primary
        isCompleted  -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        else         -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
    }
    val badgeText = when {
        isCurrentDay -> MaterialTheme.colorScheme.onPrimary
        else         -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
    }
    val labelColor = when {
        isCurrentDay -> MaterialTheme.colorScheme.primary
        isCompleted  -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        else         -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(cardAlpha)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(badgeBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$dayNum", color = badgeText, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("DAY $dayNum", color = labelColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text(
                            if (isRest) "REST & RECOVERY" else "SCHEDULED ACTIVITY",
                            color = if (isCompleted)
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            else
                                MaterialTheme.colorScheme.onBackground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            if (!isRest) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dayPlan.workoutExercises.take(3).forEach { exercise ->
                        ExerciseTag(exercise.name)
                    }
                    if (dayPlan.workoutExercises.size > 3) {
                        ExerciseTag("+${dayPlan.workoutExercises.size - 3}")
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseTag(name: String) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            name.uppercase(),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlannerScreenPreview() {
    FitflowTheme { PlannerScreen(emptyList()) }
}