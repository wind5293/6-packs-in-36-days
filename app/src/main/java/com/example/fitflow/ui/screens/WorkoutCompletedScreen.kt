package com.example.fitflow.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.UserProfile
import kotlin.math.roundToInt

@Composable
fun WorkoutCompletedScreen(
    dayPlan: DayPlan?,
    totalActiveSeconds: Int,
    userProfile: UserProfile?,
    onNext: () -> Unit
) {
    val totalExercises = dayPlan?.workoutExercises?.size ?: 0
    // Use total pre-calculated calories from the DayPlan
    val estimatedCalories = dayPlan?.workoutExercises?.sumOf { it.kcal.toDouble() } ?: 0.0
    val minutes = totalActiveSeconds / 60
    val seconds = totalActiveSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    var selectedFeedback by remember { mutableStateOf<String?>(null) }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onBackground
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // --- Header (Gradient) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(primaryColor, Color(0xFF1E1E1E))
                    )
                )
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp, vertical = 48.dp)
        ) {
            Column {
                Text(
                    text = "Workout\nCompleted!",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 42.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Day ${dayPlan?.dayNumber ?: 1} · ${dayPlan?.title ?: ""}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // --- Content ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Exercises", totalExercises.toString(), primaryColor, textColor)
                    StatItem("Calories", String.format("%.1f", estimatedCalories), primaryColor, textColor)
                    StatItem("Time", timeFormatted, primaryColor, textColor)
                }
            }

            // Feedback Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "How do you feel?",
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your feedback will help us provide more suitable workouts for you",
                        color = subTextColor,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FeedbackButton(
                            emoji = "😩",
                            label = "Too hard",
                            isSelected = selectedFeedback == "Too hard",
                            onClick = { selectedFeedback = "Too hard" },
                            textColor = textColor,
                            primaryColor = primaryColor
                        )
                        FeedbackButton(
                            emoji = "🙂",
                            label = "Just right",
                            isSelected = selectedFeedback == "Just right",
                            onClick = { selectedFeedback = "Just right" },
                            textColor = textColor,
                            primaryColor = primaryColor
                        )
                        FeedbackButton(
                            emoji = "😎",
                            label = "Too easy",
                            isSelected = selectedFeedback == "Too easy",
                            onClick = { selectedFeedback = "Too easy" },
                            textColor = textColor,
                            primaryColor = primaryColor
                        )
                    }
                }
            }

            // Weight Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Weight",
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${userProfile?.weight ?: 0.0} KG",
                        color = textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // BMI Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BMI (kg/m²)",
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val bmi = userProfile?.bmi ?: 0f
                    Text(
                        text = String.format("%.1f", bmi),
                        color = textColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    
                    val bmiCategory = userProfile?.bmiCategory?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Unknown"
                    val categoryColor = when(bmiCategory) {
                        "Underweight" -> Color(0xFF42A5F5)
                        "Normal" -> Color(0xFF66BB6A)
                        "Overweight" -> Color(0xFFFFA726)
                        else -> Color.Gray
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(categoryColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$bmiCategory weight",
                            color = textColor,
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    BmiSlider(bmi = bmi)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- Bottom Button ---
        Box(modifier = Modifier.padding(16.dp).windowInsetsPadding(WindowInsets.navigationBars)) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Next",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, primaryColor: Color, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, color = primaryColor, fontSize = 24.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun FeedbackButton(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    textColor: Color,
    primaryColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) primaryColor.copy(alpha = 0.2f) else Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(text = emoji, fontSize = 36.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = if (isSelected) primaryColor else textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun BmiSlider(bmi: Float) {
    val minBmi = 15f
    val maxBmi = 40f
    
    // Map BMI to a 0.0 - 1.0 fraction
    val fraction = ((bmi - minBmi) / (maxBmi - minBmi)).coerceIn(0f, 1f)
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(24.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = 8.dp.toPx()
                val yOffset = size.height - height
                
                // Draw gradient bar
                val brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF42A5F5), // Blue (Underweight)
                        Color(0xFF66BB6A), // Green (Normal)
                        Color(0xFFFFA726), // Orange (Overweight)
                        Color(0xFFEF5350)  // Red (Obese)
                    )
                )
                
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(0f, yOffset),
                    size = androidx.compose.ui.geometry.Size(width, height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(height/2, height/2)
                )
                
                // Draw triangle pointer
                val pointerX = width * fraction
                val trianglePath = Path().apply {
                    moveTo(pointerX, yOffset) // Bottom point (touching bar)
                    lineTo(pointerX - 8.dp.toPx(), yOffset - 12.dp.toPx()) // Top left
                    lineTo(pointerX + 8.dp.toPx(), yOffset - 12.dp.toPx()) // Top right
                    close()
                }
                
                drawPath(
                    path = trianglePath,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("15", "18.5", "25", "30", "35", "40").forEach { value ->
                Text(
                    text = value,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}
