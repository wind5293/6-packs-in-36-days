package com.example.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.ui.theme.*

@Composable
fun OnboardingScreen(onComplete: (height: Float, wight: Float) -> Unit) {
    var height by remember { mutableFloatStateOf(170f) }
    var weight by remember { mutableFloatStateOf(65f) }
    var selectedGoal by remember { mutableStateOf("Weight Loss") }

    val bmi = weight / ((height / 100) * (height / 100))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp)
            .padding(top = 40.dp)
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row {
                Text("FIT", color = TextDim, fontSize = 36.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                Text("FLOW", color = AccentNeon, fontSize = 36.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
            }
            Text("CRAFT YOUR MONTHLY JOURNEY", color = White40, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Height Slider
        Text("HEIGHT (CM)", color = White40, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = height,
            onValueChange = { height = it },
            valueRange = 120f..220f,
            colors = SliderDefaults.colors(
                thumbColor = AccentNeon,
                activeTrackColor = AccentNeon,
                inactiveTrackColor = White10
            )
        )
        Text("${height.toInt()}", color = TextDim, fontSize = 28.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(32.dp))

        // BMI Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, White10, RoundedCornerShape(32.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("CALCULATED BMI", color = White40, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(String.format("%.1f", bmi), color = TextDim, fontSize = 48.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Goals
        Text("MAIN GOAL", color = White40, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(16.dp))
        val goals = listOf("Weight Loss", "Muscle Gain", "Endurance", "Maintenance")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GoalButton(goals[0], selectedGoal == goals[0], Modifier.weight(1f)) { selectedGoal = goals[0] }
                GoalButton(goals[1], selectedGoal == goals[1], Modifier.weight(1f)) { selectedGoal = goals[1] }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GoalButton(goals[2], selectedGoal == goals[2], Modifier.weight(1f)) { selectedGoal = goals[2] }
                GoalButton(goals[3], selectedGoal == goals[3], Modifier.weight(1f)) { selectedGoal = goals[3] }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Complete Button
        Button(
            onClick = { onComplete(height, weight)},
            colors = ButtonDefaults.buttonColors(containerColor = AccentNeon),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ACTIVATE JOURNEY", color = BackgroundDark, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = BackgroundDark)
            }
        }
    }
}

@Composable
fun GoalButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bgColor = if (isSelected) AccentNeon else White05
    val textColor = if (isSelected) BackgroundDark else White40
    val borderColor = if (isSelected) AccentNeon else White10

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text.uppercase(), color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}