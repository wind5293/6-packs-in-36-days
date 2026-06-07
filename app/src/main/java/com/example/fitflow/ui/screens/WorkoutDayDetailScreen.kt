package com.example.fitflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.request.CachePolicy
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.composables.icons.lucide.*
import com.example.fitflow.R
import com.example.fitflow.FitFlowApplication
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.WorkoutExercise
import com.example.fitflow.data.model.FitnessGoal
import com.example.fitflow.domain.PushYourLimitsCatalog
import com.example.fitflow.ui.theme.FitflowTheme
import com.example.fitflow.ui.theme.OrangeGlow
import com.example.fitflow.ui.theme.OrangePrimary
import com.example.fitflow.utils.GifUrlHelper

@Composable
fun WorkoutDayDetailScreen(
    dayPlan: DayPlan,
    goal: FitnessGoal? = null,
    partialIndex: Int? = null,
    onBack: () -> Unit,
    onStartSession: (Int) -> Unit = {},
    onEditPlan: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    var selectedExercise by remember { mutableStateOf<WorkoutExercise?>(null) }
    val context = LocalContext.current
    val imageLoader = (context.applicationContext as FitFlowApplication).imageLoader
    val gifUrls = remember(dayPlan.workoutExercises) {
        dayPlan.workoutExercises.mapNotNull { exercise ->
            exercise.gifFileName
                .takeIf { it.isNotEmpty() }
                ?.let { GifUrlHelper.getUrl(it) }
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
                    goal = goal,
                    onBack = onBack,
                    onOpenSettings = onOpenSettings
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exercise",
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Plan",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onEditPlan() }
                    )
                }
            }
            itemsIndexed(dayPlan.workoutExercises) { index, exercise ->
                val isCompleted = partialIndex != null && index < partialIndex
                ExerciseExpandableItem(
                    exercise = exercise,
                    isCompleted = isCompleted,
                    onClick = { selectedExercise = exercise }
                )
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
            if (partialIndex != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { onStartSession(0) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Text(
                            text = "RESTART",
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        )
                    }
                    Button(
                        onClick = { onStartSession(partialIndex) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        val total = dayPlan.workoutExercises.size
                        val percent = if (total > 0) (partialIndex * 100) / total else 0
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CONTINUE",
                                maxLines = 1,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            )
                            Text(
                                text = "$percent% completed",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = { onStartSession(0) },
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

        if (selectedExercise != null) {
            ExerciseInstructionOverlayScreen(
                exercise = selectedExercise!!,
                onClose = { selectedExercise = null }
            )
        }
    }
}

@Composable
fun HeaderAndSummarySection(
    dayPlan: DayPlan,
    goal: FitnessGoal?,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val exercises = dayPlan.workoutExercises
    val totalKcal = exercises.sumOf { it.kcal }
    val duration = exercises.sumOf {
        if (it.durationSec > 0) it.durationSec
        else it.sets * 45
    } / 60

    val supplementary = PushYourLimitsCatalog.all().find { it.title == dayPlan.title }
    
    val imageRes = if (supplementary != null) {
        supplementary.imageRes ?: R.drawable.co_bung_2
    } else {
        when (goal) {
            FitnessGoal.WEIGHT_LOSS -> R.drawable.cobap2
            FitnessGoal.MUSCLE_GAIN -> R.drawable.cobap1
            FitnessGoal.ENDURANCE -> R.drawable.cobap3
            FitnessGoal.MAINTENANCE -> R.drawable.cobap4
            else -> R.drawable.co_bung_2
        }
    }

    val bgBrush: Brush = if (supplementary != null) {
        if (supplementary.isFullBackground) {
            androidx.compose.ui.graphics.SolidColor(supplementary.gradientStart)
        } else {
            Brush.horizontalGradient(listOf(supplementary.gradientStart, supplementary.gradientEnd))
        }
    } else {
        Brush.verticalGradient(listOf(OrangePrimary, OrangeGlow))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            // ── Supplementary: full-bleed image like the card ──
            if (supplementary != null && supplementary.imageRes != null) {
                Image(
                    painter = painterResource(id = supplementary.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (supplementary.mirrorImage) Modifier.graphicsLayer(scaleX = -1f)
                            else Modifier
                        )
                )
                // Gradient overlay same as card
                if (supplementary.isFullBackground) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    0.0f to supplementary.gradientStart,
                                    0.5f to supplementary.gradientStart.copy(alpha = 0.6f),
                                    1.0f to Color.Transparent
                                )
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        supplementary.gradientStart.copy(alpha = 0.85f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            } else {
                // Daily challenge: colored background + athlete image on right
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgBrush)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .fillMaxHeight()
                        .width(220.dp)
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "Workout Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Text overlay — positioned top-start, below the back button row
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(horizontal = 20.dp)
                    .padding(top = 60.dp)
            ) {
                val mainTitle = if (dayPlan.dayNumber == 0) dayPlan.title else "Day ${dayPlan.dayNumber}"
                if (supplementary != null) {
                    Text(
                        text = supplementary.muscleGroup.uppercase(),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = supplementary.title,
                        color = supplementary.textColor,
                        fontSize = 22.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${supplementary.difficulty} • ${supplementary.durationMinutes} Min",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = mainTitle,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Back button top-left
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        tint = Color.White,
                        contentDescription = "back"
                    )
                }
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
                    SummaryItem(value = "$totalKcal kcal(≈)", label = "Calories")
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenSettings() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Workout Settings",
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Music & Coach & Timer, etc.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Workout Settings",
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
private fun ExerciseExpandableItem(
    exercise: WorkoutExercise,
    isCompleted: Boolean = false,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val imageLoader = (context.applicationContext as FitFlowApplication).imageLoader

    val gifUrl = remember(exercise.gifFileName) {
        exercise.gifFileName
            .takeIf { it.isNotEmpty() }
            ?.let { GifUrlHelper.getUrl(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
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

            IconButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand details",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        // Phần chi tiết xổ xuống
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
            dayPlan = DayPlan(1, false, sampleExercises),
            goal = null,
            onBack = {},
            onStartSession = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderAndSummarySectionPreview() {
    FitflowTheme {
        HeaderAndSummarySection(
            dayPlan = DayPlan(1, false, sampleExercises),
            goal = null,
            onBack = {  },
            onOpenSettings = {  }
        )
    }
}
