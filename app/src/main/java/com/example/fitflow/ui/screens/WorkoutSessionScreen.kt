package com.example.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.example.fitflow.ui.theme.*

data class Exercise(val name: String, val durationSec: Int)

@Composable
fun WorkoutSessionScreen(
	exercises: List<Exercise> = sampleExercises(),
	onBack: () -> Unit = {},
	onFinish: () -> Unit = {}
) {
	var index by remember { mutableStateOf(0) }
	var remaining by remember { mutableStateOf(exercises.getOrNull(0)?.durationSec ?: 0) }
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
			// advance to next exercise
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
			.background(BackgroundDark)
			.padding(20.dp)
	) {
		TopAppBar(
			title = { Text("Workout Session", color = TextDim) },
			navigationIcon = {
				IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AccentNeon) }
			},
			colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = BackgroundDark)
		)

		Spacer(Modifier.height(12.dp))

		// Timer and current exercise
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp),
			colors = CardDefaults.cardColors(containerColor = White05)
		) {
			Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
				// circular progress + timer
				Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
					CircularProgressIndicator(
						progress = progress,
						modifier = Modifier.fillMaxSize(),
						color = AccentNeon,
						strokeWidth = 8.dp
					)
					Column(horizontalAlignment = Alignment.CenterHorizontally) {
						Text(remaining.toString(), color = AccentNeon, fontSize = 28.sp, fontWeight = FontWeight.Black)
						Text("sec", color = TextDim, fontSize = 12.sp)
					}
				}

				Spacer(Modifier.width(16.dp))

				Column(modifier = Modifier.weight(1f)) {
					Text(current?.name ?: "Rest", color = TextDim, fontSize = 18.sp, fontWeight = FontWeight.Bold)
					Spacer(Modifier.height(8.dp))
					LinearProgressIndicator(progress = (index.toFloat() / (exercises.size).coerceAtLeast(1)),
						color = AccentNeon, trackColor = White10, modifier = Modifier.fillMaxWidth().height(6.dp))
					Spacer(Modifier.height(8.dp))
					Text("Set ${index + 1} of ${exercises.size}", color = White40, fontSize = 12.sp)
				}
			}
		}

		Spacer(Modifier.height(12.dp))

		// Controls
		Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
			IconButton(onClick = {
				isRunning = !isRunning
			}, modifier = Modifier
				.size(64.dp)
				.clip(CircleShape)
				.background(White05)) {
				if (isRunning) Icon(Icons.Default.Pause, contentDescription = "Pause", tint = AccentNeon)
				else Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = AccentNeon)
			}

			IconButton(onClick = {
				// skip
				if (index < exercises.lastIndex) {
					index += 1
					remaining = exercises[index].durationSec
					isRunning = true
				} else {
					// finish
					isRunning = false
					onFinish()
				}
			}, modifier = Modifier
				.size(64.dp)
				.clip(CircleShape)
				.background(White05)) {
				Icon(Icons.Default.SkipNext, contentDescription = "Skip", tint = AccentNeon)
			}
		}

		Spacer(Modifier.height(18.dp))

		// Upcoming exercises
		Text("UPCOMING", color = White40, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
		Spacer(Modifier.height(8.dp))
		Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
			exercises.drop(index + 1).take(5).forEachIndexed { i, ex ->
				Row(modifier = Modifier
					.fillMaxWidth()
					.clip(MaterialTheme.shapes.medium)
					.background(White05)
					.padding(12.dp)) {
					Column(modifier = Modifier.weight(1f)) {
						Text(ex.name, color = TextDim, fontSize = 14.sp, fontWeight = FontWeight.Bold)
						Text("${ex.durationSec} sec", color = White40, fontSize = 12.sp)
					}
					Text("#${index + 2 + i}", color = White20, modifier = Modifier.align(Alignment.CenterVertically))
				}
			}
		}

		Spacer(Modifier.weight(1f))

	}
}

fun sampleExercises() = listOf(
	Exercise("Jumping Jacks", 30),
	Exercise("Push Ups", 40),
	Exercise("Bodyweight Squats", 45),
	Exercise("Plank Hold", 60),
	Exercise("Lunges", 40)
)

@Preview(showBackground = true)
@Composable
fun WorkoutSessionScreenPreview() {
	FitflowTheme { WorkoutSessionScreen() }
}

