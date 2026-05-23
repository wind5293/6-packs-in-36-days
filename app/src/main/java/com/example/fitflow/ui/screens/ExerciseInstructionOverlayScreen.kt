package com.example.fitflow.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.fitflow.FitFlowApplication
import com.example.fitflow.data.model.Exercise
import com.example.fitflow.data.model.WorkoutExercise

private enum class InstructionMode { ANIMATION, GUIDANCE }

@Composable
fun ExerciseInstructionOverlayScreen(
    exercise: WorkoutExercise,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember {
        (context.applicationContext as FitFlowApplication).exerciseRepository
    }

    var detail by remember(exercise.name) { mutableStateOf<Exercise?>(null) }
    var mode by remember { mutableIntStateOf(0) }

    LaunchedEffect(exercise.name) {
        detail = repository.findBestMatchByName(exercise.name)
    }

    val modes = listOf("HOAT HINH", "HUONG DAN")
    val selectedMode = if (mode == 0) InstructionMode.ANIMATION else InstructionMode.GUIDANCE

    val gifPath = run {
        val fromJson = detail?.local_gifs?.firstOrNull()
        val fromPlan = exercise.localGifs.firstOrNull()
        val filename = fromJson ?: fromPlan
        if (!filename.isNullOrBlank()) "gifs/$filename" else null
    }

    var hasGif by remember(gifPath) { mutableStateOf(false) }
    LaunchedEffect(gifPath) {
        hasGif = try {
            if (gifPath == null) false else context.assets.open(gifPath).use { true }
        } catch (_: Exception) {
            false
        }
    }

    val targetMuscles = detail?.target_muscles
        ?.filter { it.lowercase() != "main" }
        ?.distinct()
        .orEmpty()

    val instructions = detail?.instructions.orEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "EXERCISE DETAIL",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = exercise.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ScrollableTabRow(selectedTabIndex = mode) {
                modes.forEachIndexed { index, label ->
                    Tab(
                        selected = mode == index,
                        onClick = { mode = index },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (mode == index) FontWeight.Black else FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "TARGET MUSCLES",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (targetMuscles.isEmpty()) {
                        AssistChip(onClick = {}, enabled = false, label = { Text("N/A") })
                    } else {
                        targetMuscles.forEach { muscle ->
                            AssistChip(onClick = {}, label = { Text(muscle.uppercase()) })
                        }
                    }
                }

                if (selectedMode == InstructionMode.ANIMATION) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasGif && gifPath != null) {
                                val imageLoader = remember {
                                    ImageLoader.Builder(context).components {
                                        if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                                        else add(GifDecoder.Factory())
                                    }.build()
                                }
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data("file:///android_asset/$gifPath")
                                        .build(),
                                    imageLoader = imageLoader,
                                    contentDescription = "Exercise animation",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = "No animation available",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = {},
                            label = {
                                val info = if (exercise.reps > 0) "x ${exercise.reps}" else "${exercise.durationSec}s"
                                Text(info)
                            }
                        )
                        AssistChip(onClick = {}, label = { Text("SETS ${exercise.sets}") })
                    }
                } else {
                    Text(
                        text = "INSTRUCTIONS",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    if (instructions.isEmpty()) {
                        Text(
                            text = "Instruction is not available for this exercise yet.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    } else {
                        instructions.forEachIndexed { index, line ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(24.dp)
                                )
                                Text(
                                    text = line,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
