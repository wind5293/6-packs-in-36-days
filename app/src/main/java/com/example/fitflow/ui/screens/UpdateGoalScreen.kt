package com.example.fitflow.ui.screens

import androidx.activity.ComponentActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitflow.data.model.FitnessGoal
import com.example.fitflow.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateGoalScreen(
    currentGoal: FitnessGoal,
    onBack: () -> Unit,
    // resetProgress = true → clear old data and start Day 1
    // resetProgress = false → restore frozen progress and continue
    onComplete: (goal: FitnessGoal, resetProgress: Boolean) -> Unit
) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel(context as ComponentActivity)

    var selectedGoal by remember {
        mutableStateOf(
            FitnessGoal.entries.firstOrNull { it != currentGoal } ?: FitnessGoal.WEIGHT_LOSS
        )
    }

    // Dialog state
    var showResumeDialog by remember { mutableStateOf(false) }
    var resumeDialogGoal by remember { mutableStateOf<FitnessGoal?>(null) }
    var resumeDialogCount by remember { mutableStateOf(0) }

    // Resume dialog — shown when selectedGoal has prior frozen progress
    if (showResumeDialog && resumeDialogGoal != null) {
        AlertDialog(
            onDismissRequest = { showResumeDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Khôi phục tiến trình?",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    "Bạn đã hoàn thành $resumeDialogCount ngày của kế hoạch " +
                    "${resumeDialogGoal!!.title} trước đó.\n\n" +
                    "Bạn muốn tập tiếp Day ${resumeDialogCount + 1} hay bắt đầu lại từ Day 1?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    lineHeight = 20.sp
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showResumeDialog = false
                        onComplete(resumeDialogGoal!!, true) // reset → Day 1
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text(
                        "Day 1",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResumeDialog = false
                        onComplete(resumeDialogGoal!!, false) // resume → keep progress
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Tiếp tục Day ${resumeDialogCount + 1}",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
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
                        fontStyle = FontStyle.Normal
                    )
                    Text(
                        "UPDATE",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                "CHOOSE YOUR NEW GOAL",
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

            // Info card: current goal label
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "CURRENT GOAL",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        currentGoal.title,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        currentGoal.description,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
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
                onClick = {
                    val count = userViewModel.getCompletedCountForGoal(selectedGoal)
                    if (count > 0) {
                        // Show resume dialog — user decides Day 1 or continue
                        resumeDialogGoal = selectedGoal
                        resumeDialogCount = count
                        showResumeDialog = true
                    } else {
                        // No prior progress → go straight to Day 1
                        onComplete(selectedGoal, true)
                    }
                },
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
