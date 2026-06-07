package com.example.fitflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.fitflow.data.model.SupplementaryWorkout
import com.example.fitflow.domain.PushYourLimitsCatalog
import java.util.Locale


@Composable
fun PushYourLimitsSection(
    workouts: List<SupplementaryWorkout>,
    onWorkoutClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Push Your Limits",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "MUSCLE TARGET",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(14.dp))
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            workouts.forEach { workout ->
                PushYourLimitsCard(
                    workout = workout,
                    onClick = { onWorkoutClick(workout.id) }
                )
            }
        }
    }
}

private fun cardMetaText(workout: SupplementaryWorkout): String {
    return "${workout.difficulty} • ${workout.durationMinutes} Min"
}

private fun focusMuscleTags(workout: SupplementaryWorkout): List<String> {
    return when (workout.muscleGroup.lowercase(Locale.ROOT)) {
        "core" -> listOf("Abs", "Oblique", "Lower Core")
        "upper push" -> listOf("Chest", "Shoulder", "Triceps")
        "lower body" -> listOf("Quads", "Glutes", "Hamstrings")
        "back" -> listOf("Lats", "Mid Back", "Rear Delt")
        "full body" -> listOf("Core", "Upper", "Lower")
        else -> listOf(workout.muscleGroup)
    }
}

@Composable
private fun PushYourLimitsCard(
    workout: SupplementaryWorkout,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .width(300.dp)
            .height(160.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (workout.isFullBackground) Modifier.background(workout.gradientStart)
                    else Modifier.background(Brush.horizontalGradient(listOf(workout.gradientStart, workout.gradientEnd)))
                )
        ) {
            // Background Image
            if (workout.imageRes != null) {
                if (workout.isFullBackground) {
                    Image(
                        painter = painterResource(id = workout.imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = workout.imageOffsetDp.dp)
                            .then(
                                if (workout.mirrorImage) Modifier.graphicsLayer(scaleX = -1f) 
                                else Modifier
                            )
                    )
                    // Gradient overlay to fade left side into the background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    0.0f to workout.gradientStart,
                                    0.55f to workout.gradientStart,
                                    1.0f to workout.gradientEnd
                                )
                            )
                    )
                } else {
                    // Anatomy style image on the right
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(140.dp)
                            .align(Alignment.CenterEnd)
                    ) {
                        Image(
                            painter = painterResource(id = workout.imageRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Subtle gradient to blend the left edge of the image
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(workout.gradientStart.copy(alpha = 0.5f), Color.Transparent),
                                        startX = 0f,
                                        endX = 100f
                                    )
                                )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, top = 20.dp, bottom = 20.dp, end = 140.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = workout.title,
                        color = workout.textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = cardMetaText(workout),
                        color = workout.textColor.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = workout.buttonBgColor,
                        contentColor = workout.buttonTextColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "START",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPushYourLimitsCardFull() {
    val sampleWorkout = PushYourLimitsCatalog.findById("upper_push")
    if (sampleWorkout != null) {
        Box(modifier = Modifier.padding(16.dp)) {
            PushYourLimitsCard(workout = sampleWorkout, onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPushYourLimitsCardAnatomy() {
    val sampleWorkout = PushYourLimitsCatalog.findById("core_abs")
    if (sampleWorkout != null) {
        Box(modifier = Modifier.padding(16.dp)) {
            PushYourLimitsCard(workout = sampleWorkout, onClick = {})
        }
    }
}
