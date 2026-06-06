package com.example.fitflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.fitflow.data.model.SupplementaryWorkout
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
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(workouts, key = { it.id }) { workout ->
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
    val shape = RoundedCornerShape(24.dp)
    val tags = focusMuscleTags(workout)

    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .widthIn(min = 288.dp, max = 332.dp)
            .heightIn(min = 186.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            workout.gradientStart,
                            workout.gradientEnd
                        )
                    )
                )
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(0.62f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FOCUS: ${workout.muscleGroup.uppercase(Locale.ROOT)}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = workout.title,
                        color = Color.White,
                        fontSize = 17.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = workout.subtitle,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        tags.take(2).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = tag,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = cardMetaText(workout),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.Black.copy(alpha = 0.18f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (workout.imageRes != null) {
                    Image(
                        painter = painterResource(id = workout.imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(Color.Black.copy(alpha = 0.38f))
//                        .padding(horizontal = 8.dp, vertical = 6.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = workout.muscleGroup,
//                        color = Color.White,
//                        fontSize = 11.sp,
//                        fontWeight = FontWeight.Bold,
//                        textAlign = TextAlign.Center,
//                        maxLines = 1,
//                        modifier = Modifier.fillMaxWidth(),
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
            }
        }
    }
}
