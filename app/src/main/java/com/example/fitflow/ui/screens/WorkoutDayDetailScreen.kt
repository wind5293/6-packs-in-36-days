package com.example.fitflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.Exercise
import com.example.fitflow.ui.theme.FitflowTheme
import com.example.fitflow.ui.theme.OrangeGlow
import com.example.fitflow.ui.theme.OrangePrimary

@Composable
fun WorkoutDayDetailScreen(
    dayPlan: DayPlan,
    onBack: () -> Unit,
    onStartSession: () -> Unit = {}
) {
    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                HeaderAndSummarySection(1, dayPlan.exercises, onBack)
            }
            items(dayPlan.exercises.size) { index ->
                ExerciseExpandableItem(dayPlan.exercises[index])
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
                .padding(24.dp)
        ) {
            Button(
                onClick = onStartSession,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "START",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )
            }
        }

    }
}

@Composable
fun HeaderAndSummarySection(
    dayNumber: Int,
    exercises: List<Exercise>,
    onBack: () -> Unit
) {
    val exercisesCount = exercises.size
    val totalKcal = exercises.sumOf { it.kcal }
    val duration = exercises.sumOf { it.durationSec }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
    ) {
        Box (
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(OrangePrimary, OrangeGlow)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            tint = Color(0xFFFFFFFF),
                            contentDescription = "back")
                    }
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            tint = Color(0xFFFFFFFF),
                            contentDescription = "Menu")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Day $dayNumber",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 35.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚡⚡⚡ Intermediate",
                    color = Color(0xFFFFFFFF),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp)
                    .width(120.dp)
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Image Space",
                    color = MaterialTheme.colorScheme.background,
                    fontSize = 12.sp
                )
            }
        }
        Card (
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomEnd),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryItem(value = "$exercisesCount", label = "Exercises")
                    SummaryItem(value = "$duration min", label = "Time")
                    SummaryItem(value = "$totalKcal", label = "Calories")
                }
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Workout Settings",
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Music & Coach & Timer, etc.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryItem(
    value: String,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExerciseExpandableItem(exercise: Exercise) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Text("IMG", fontSize = 10.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Thông tin chính
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 22.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                val subText = if (exercise.durationSec > 0) {
                    val m = exercise.durationSec / 60
                    val s = exercise.durationSec % 60
                    String.format("%02d:%02d", m, s)
                } else {
                    "x ${exercise.reps}"
                }
                Text(
                    text = subText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
        }

        // Phần chi tiết xổ xuống khi bấm vào
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 76.dp) // Canh lề cho khớp với phần text ở trên
            ) {
                Text(
                    text = "Category: ${exercise.category}",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Text(text = "Sets: ${exercise.sets}", fontSize = 12.sp, color = Color.DarkGray)
                Text(text = "Burn: ${exercise.kcal} kcal", fontSize = 12.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            }
        }
    }
}

 val sampleExercises = listOf(
    Exercise(category = "Cardio", name = "Jumping Jacks", sets = 1, reps = 0, kcal = 10, durationSec = 30),
    Exercise(category = "Strength", name = "Push Ups", sets = 1, reps = 0, kcal = 15, durationSec = 40),
    Exercise(category = "Strength", name = "Bodyweight Squats", sets = 1, reps = 0, kcal = 20, durationSec = 45),
    Exercise(category = "Core", name = "Plank Hold", sets = 1, reps = 0, kcal = 10, durationSec = 60),
    Exercise(category = "Strength", name = "Lunges", sets = 1, reps = 0, kcal = 15, durationSec = 40)
)

@Preview(showBackground = true)
@Composable
fun WorkoutDayDetailScreenPreview() {
    FitflowTheme {
        WorkoutDayDetailScreen(
            DayPlan(1, false, sampleExercises()),
            {},
            {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderAndSummarySectionPreview() {
    FitflowTheme {
        HeaderAndSummarySection(
            dayNumber = 1,
            exercises = sampleExercises,
            onBack = {  }
        )
    }
}

