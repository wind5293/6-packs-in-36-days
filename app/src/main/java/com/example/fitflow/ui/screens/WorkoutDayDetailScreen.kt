package com.example.fitflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.request.CachePolicy
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fitflow.FitFlowApplication
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.WorkoutExercise
import com.example.fitflow.ui.theme.FitflowTheme
import com.example.fitflow.ui.theme.OrangeGlow
import com.example.fitflow.ui.theme.OrangePrimary
import com.example.fitflow.utils.GifSourceResolver

@Composable
fun WorkoutDayDetailScreen(
    dayPlan: DayPlan,
    onBack: () -> Unit,
    onStartSession: () -> Unit = {},
    onEditPlan: () -> Unit = {}
) {
    val context = LocalContext.current
    val imageLoader = (context.applicationContext as FitFlowApplication).imageLoader
    val gifUrls = remember(dayPlan.workoutExercises) {
        dayPlan.workoutExercises.mapNotNull { exercise ->
            exercise.gifFileName
                .takeIf { it.isNotEmpty() }
                ?.let { GifSourceResolver.resolve(it, context) }
        }
    }

    LaunchedEffect(dayPlan.dayNumber, gifUrls) {
        // Warm up memory/disk cache before list items are visible.
        gifUrls.take(8).forEach { url ->
            imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(url)
                    .memoryCacheKey(url)
                    .diskCacheKey(url)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
            )
        }
    }

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
                HeaderAndSummarySection(
                    dayPlan = dayPlan,
                    onBack = onBack,
                    onEditPlan = onEditPlan
                )
            }
            items(dayPlan.workoutExercises) { exercise ->
                ExerciseExpandableItem(exercise)
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
    dayPlan: DayPlan,
    onBack: () -> Unit,
    onEditPlan: () -> Unit
) {
    val exercises = dayPlan.workoutExercises
    val totalKcal = exercises.sumOf { it.kcal }
    val duration = exercises.sumOf {
        if (it.durationSec > 0) it.durationSec
        else it.sets * 45
    } / 60

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-8).dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            tint = Color(0xFFFFFFFF),
                            contentDescription = "back")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Day ${dayPlan.dayNumber}",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 35.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
                if (dayPlan.title.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayPlan.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val dots = when (dayPlan.difficulty.lowercase()) {
                    "easy"     -> "⚡"
                    "advanced" -> "⚡⚡⚡"
                    else       -> "⚡⚡"
                }
                Text(
                    text = "$dots ${dayPlan.difficulty}",
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
                    text = dayPlan.muscleGroup.ifEmpty { "Image Space" },
                    color = MaterialTheme.colorScheme.background,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
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
                    SummaryItem(value = "${exercises.size}", label = "Exercises")
                    SummaryItem(value = "$duration min", label = "Time")
                    SummaryItem(value = "$totalKcal", label = "Calories")
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEditPlan() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Edit Workout",
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Add, remove or reorder exercises",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Plan",
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
private fun ExerciseExpandableItem(exercise: WorkoutExercise) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val imageLoader = (context.applicationContext as FitFlowApplication).imageLoader

    val gifUrl = remember(exercise.gifFileName) {
        exercise.gifFileName
            .takeIf { it.isNotEmpty() }
            ?.let { GifSourceResolver.resolve(it, context) }
    }

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
                if (gifUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(gifUrl)
                            .crossfade(false)
                            .memoryCacheKey(gifUrl)
                            .diskCacheKey(gifUrl)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = exercise.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
                val textColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                Text(
                    text = "Category: ${exercise.category}",
                    fontSize = 12.sp,
                    color = textColor
                )
                Text(text = "Sets: ${exercise.sets}", fontSize = 12.sp, color = textColor)
                Text(text = "Burn: ${exercise.kcal} kcal", fontSize = 12.sp, color = textColor)
                if (exercise.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Description: ${exercise.description}",
                        fontSize = 12.sp,
                        color = textColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            }
        }
    }
}

private val sampleExercises = listOf(
    WorkoutExercise(category = "Cardio", name = "Jumping Jacks", sets = 1, reps = 0, kcal = 10, durationSec = 30),
    WorkoutExercise(category = "Strength", name = "Push Ups", sets = 1, reps = 0, kcal = 15, durationSec = 40),
    WorkoutExercise(category = "Strength", name = "Bodyweight Squats", sets = 1, reps = 0, kcal = 20, durationSec = 45),
    WorkoutExercise(category = "Core", name = "Plank Hold", sets = 1, reps = 0, kcal = 10, durationSec = 60),
    WorkoutExercise(category = "Strength", name = "Lunges", sets = 1, reps = 0, kcal = 15, durationSec = 40)
)

@Preview(showBackground = true)
@Composable
fun WorkoutDayDetailScreenPreview() {
    FitflowTheme {
        WorkoutDayDetailScreen(
            DayPlan(1, false, sampleExercises),
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
            dayPlan = DayPlan(1, false, sampleExercises),
            onBack = {  },
            onEditPlan = {  }
        )
    }
}
