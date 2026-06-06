package com.example.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.data.model.FitnessGoal

@Composable
fun UpdateGoalScreen(
    currentGoal: FitnessGoal,
    onBack: () -> Unit,
    onComplete: (FitnessGoal) -> Unit
) {
    var selectedGoal by remember { 
        // Default to the first available goal that is not the current one
        mutableStateOf(
            FitnessGoal.values().firstOrNull { it != currentGoal } ?: FitnessGoal.WEIGHT_LOSS
        ) 
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row {
                    Text(
                        "FITNESS GOAL ",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        "UPDATE",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                "FITNESS GOAL",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (currentGoal != FitnessGoal.WEIGHT_LOSS) {
                    EquipmentItem(
                        "Weight Loss",
                        "Cardio-focused · Burn calories",
                        selectedGoal == FitnessGoal.WEIGHT_LOSS
                    ) { selectedGoal = FitnessGoal.WEIGHT_LOSS }
                }
                if (currentGoal != FitnessGoal.MUSCLE_GAIN) {
                    EquipmentItem(
                        "Muscle Gain",
                        "Strength-focused · Build muscle",
                        selectedGoal == FitnessGoal.MUSCLE_GAIN
                    ) { selectedGoal = FitnessGoal.MUSCLE_GAIN }
                }
                if (currentGoal != FitnessGoal.ENDURANCE) {
                    EquipmentItem(
                        "Endurance",
                        "Mixed training · Increase stamina",
                        selectedGoal == FitnessGoal.ENDURANCE
                    ) { selectedGoal = FitnessGoal.ENDURANCE }
                }
                if (currentGoal != FitnessGoal.MAINTENANCE) {
                    EquipmentItem(
                        "Maintenance",
                        "Balanced workout · Stay fit",
                        selectedGoal == FitnessGoal.MAINTENANCE
                    ) { selectedGoal = FitnessGoal.MAINTENANCE }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Button(
                onClick = { onComplete(selectedGoal) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "FINALIZE PROTOCOL",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
