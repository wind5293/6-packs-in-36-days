package com.example.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.ui.theme.FitflowTheme
import com.example.fitflow.data.model.Exercise
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    exercises: List<Exercise> = sampleExercises(),
    onBack: () -> Unit = {},
    onFinish: () -> Unit = {}
) {
    var index by remember { mutableStateOf(0) }
    var remaining: Int by remember { mutableStateOf(exercises.getOrNull(0)?.durationSec ?: 0) }
    var isRunning by remember { mutableStateOf(false) }

    val current = exercises.getOrNull(index)
    val totalSeconds = current?.durationSec ?: 1
    val progress = if (totalSeconds > 0) (remaining.toFloat() / totalSeconds) else 0f

    LaunchedEffect(key1 = index, key2 = isRunning) {
        if (!isRunning) return@LaunchedEffect
        while (isActive && remaining > 0) {
            delay(1000L)
            remaining = (remaining - 1).coerceAtLeast(0)
        }
        if (remaining == 0 && isRunning) {
            if (index < exercises.lastIndex) {
                index += 1
                remaining = exercises[index].durationSec
            } else {
                isRunning = false
                onFinish()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    "WORKOUT SESSION", color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack, contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 8.dp,
                        trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            remaining.toString(), color = MaterialTheme.colorScheme.primary,
                            fontSize = 28.sp, fontWeight = FontWeight.Black
                        )
                        Text(
                            "sec",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        current?.name ?: "Rest", color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { index.toFloat() / exercises.size.coerceAtLeast(1) },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Set ${index + 1} of ${exercises.size}",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = { isRunning = !isRunning },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
            ) {
                if (isRunning)
                    Icon(
                        Icons.Default.Pause, contentDescription = "Pause",
                        tint = MaterialTheme.colorScheme.primary
                    )
                else
                    Icon(
                        Icons.Default.PlayArrow, contentDescription = "Start",
                        tint = MaterialTheme.colorScheme.primary
                    )
            }

            IconButton(
                onClick = {
                    if (index < exercises.lastIndex) {
                        index += 1
                        remaining = exercises[index].durationSec
                        isRunning = true
                    } else {
                        isRunning = false
                        onFinish()
                    }
                },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
            ) {
                Icon(
                    Icons.Default.SkipNext, contentDescription = "Skip",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Text(
            "UPCOMING",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            exercises.drop(index + 1).take(5).forEachIndexed { i, ex ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            ex.name, color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp, fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${ex.durationSec} sec",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        "#${index + 2 + i}",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

fun sampleExercises() = listOf(
    Exercise(category = "Cardio", name = "Jumping Jacks", sets = 1, reps = 0, kcal = 10, durationSec = 30),
    Exercise(category = "Strength", name = "Push Ups", sets = 1, reps = 0, kcal = 15, durationSec = 40),
    Exercise(category = "Strength", name = "Bodyweight Squats", sets = 1, reps = 0, kcal = 20, durationSec = 45),
    Exercise(category = "Core", name = "Plank Hold", sets = 1, reps = 0, kcal = 10, durationSec = 60),
    Exercise(category = "Strength", name = "Lunges", sets = 1, reps = 0, kcal = 15, durationSec = 40)
)

@Preview(showBackground = true)
@Composable
fun WorkoutSessionScreenPreview() {
    FitflowTheme { WorkoutSessionScreen() }
}
