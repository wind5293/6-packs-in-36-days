package com.example.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(onComplete: (height: Float, weight: Float) -> Unit) {
    var height by remember { mutableFloatStateOf(170f) }
    var weight by remember { mutableFloatStateOf(65f) }

    val bmi = weight / ((height / 100f) * (height / 100f))

    val goalColor = when {
        bmi < 18.5f -> MaterialTheme.colorScheme.secondary
        else        -> MaterialTheme.colorScheme.primary
    }
    val suggestedGoal = when {
        bmi < 18.5f -> "MUSCLE GAIN"
        bmi < 25.0f -> "LOSE FAT + GAIN MUSCLE"
        else        -> "WEIGHT LOSS"
    }
    val bmiLabel = when {
        bmi < 18.5f -> "UNDERWEIGHT"
        bmi < 25.0f -> "NORMAL"
        else        -> "OVERWEIGHT"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Text("FIT",  color = MaterialTheme.colorScheme.onBackground, fontSize = 32.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                Text("FLOW", color = MaterialTheme.colorScheme.primary,      fontSize = 32.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
            }
            Text(
                "CRAFT YOUR MONTHLY JOURNEY",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp, modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("HEIGHT (CM)", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = height,
            onValueChange = { height = it },
            valueRange = 120f..220f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            )
        )
        Text(
            "${height.toInt()} cm",
            color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic, modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("WEIGHT (KG)", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = weight,
            onValueChange = { weight = it },
            valueRange = 30f..150f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            )
        )
        Text(
            "${weight.toInt()} kg",
            color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic, modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("CALCULATED BMI", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    String.format("%.1f", bmi),
                    color = MaterialTheme.colorScheme.onBackground, fontSize = 52.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic
                )
                Box(
                    modifier = Modifier
                        .background(goalColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .border(1.dp, goalColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(bmiLabel, color = goalColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("SUGGESTED GOAL", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(suggestedGoal, color = goalColor, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onComplete(height, weight) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ACTIVATE JOURNEY", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
