package com.example.fitflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.fitflow.data.model.FitnessGoal
import com.example.fitflow.ui.theme.FitflowTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun OnboardingScreen(onComplete: (selectedGoal: FitnessGoal, height: Float, weight: Float, birthYear: Int, targetWeight: Float) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()

    // State chung
    var selectedGoal by remember { mutableStateOf(FitnessGoal.WEIGHT_LOSS) }
    var height by remember { mutableFloatStateOf(170f) }
    var weight by remember { mutableFloatStateOf(65f) }
    var birthYear by remember { mutableIntStateOf(2000) }
    var targetWeight by remember { mutableFloatStateOf(60f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 32.dp, bottom = 24.dp)
    ) {
        // --- HEADER & PROGRESS ---
        OnboardingHeader(currentStep = pagerState.currentPage, totalSteps = 5)

        // --- PAGER CONTENT ---
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) { page ->
            when (page) {
                0 -> GoalStep(selectedGoal) { selectedGoal = it }
                1 -> BirthYearStep(birthYear) { birthYear = it }
                2 -> HeightStep(height) { height = it }
                3 -> WeightStep(weight, height, selectedGoal, { weight = it },)
                4 -> TargetWeightStep(targetWeight, weight) { targetWeight = it }
            }
        }

        // --- BOTTOM BUTTON ---
        PaddingBox {
            Button(
                onClick = {
                    if (pagerState.currentPage < 4) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onComplete(
                            selectedGoal,
                            height,
                            weight,
                            birthYear,
                            targetWeight
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    if (pagerState.currentPage == 4) "ACTIVATE JOURNEY" else "CONTINUE",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun OnboardingHeader(currentStep: Int, totalSteps: Int) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "FIT",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                fontStyle = FontStyle.Italic
            )
            Text(
                "FLOW",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                fontStyle = FontStyle.Italic
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index <= currentStep)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(0.1f)
                        )
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun GoalStep(selectedGoal: FitnessGoal, onGoalSelected: (FitnessGoal) -> Unit) {
    StepLayout(title = "WHAT IS YOUR GOAL?", subtitle = "SELECT ONE OPTION") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FitnessGoal.values().forEach { goal ->
                val isSelected = goal == selectedGoal
                Surface(
                    onClick = { onGoalSelected(goal) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(
                                goal.description,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RadioButton(selected = isSelected, onClick = null)
                    }
                }
            }
        }
    }
}

@Composable
fun BirthYearStep(birthYear: Int, onYearChange: (Int) -> Unit) {
    StepLayout(title = "WHEN WERE YOU BORN?", subtitle = "BIRTH YEAR") {
        Slider(
            value = birthYear.toFloat(),
            onValueChange = { onYearChange(it.toInt()) },
            valueRange = 1950f..2024f
        )
        Text(
            "$birthYear",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// --- STEPS UI ---
@Composable
fun HeightStep(height: Float, onHeightChange: (Float) -> Unit) {
    StepLayout(title = "HOW TALL ARE YOU?", subtitle = "HEIGHT (CM)") {
        Slider(
            value = height,
            onValueChange = onHeightChange,
            valueRange = 120f..220f
        )
        Text(
            "${height.toInt()} cm",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun WeightStep(
    weight: Float,
    height: Float,
    goal: FitnessGoal,
    onWeightChange: (Float) -> Unit,
) {
    val bmi = weight / ((height / 100f) * (height / 100f))

    StepLayout(title = "WHAT'S YOUR WEIGHT?", subtitle = "CURRENT WEIGHT (KG)") {
        Slider(
            value = weight,
            onValueChange = onWeightChange,
            valueRange = 30f..150f
        )
        Text(
            "${String.format("%.1f", weight)} kg",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        // BMI Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                    0.3f
                )
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("ADVICE FOR ${goal.title}", fontWeight = FontWeight.Black, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                val advice = when (goal) {
                    FitnessGoal.WEIGHT_LOSS -> "Focus on a calorie deficit and high-intensity training."
                    FitnessGoal.MUSCLE_GAIN -> "Prioritize protein intake and progressive overload."
                    FitnessGoal.ENDURANCE -> "Incorporate consistent zone 2 cardio sessions."
                    FitnessGoal.MAINTENANCE -> "Balanced nutrition and lifestyle consistency."
                }
                Text(advice, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun TargetWeightStep(targetWeight: Float, currentWeight: Float, onTargetChange: (Float) -> Unit) {
    val weightDiff = targetWeight - currentWeight
    val diffPercentage = (abs(weightDiff) / currentWeight) * 100
    val isLosing = weightDiff < 0

    StepLayout(title = "GOAL WEIGHT?", subtitle = "TARGET WEIGHT (KG)") {
        Slider(
            value = targetWeight,
            onValueChange = onTargetChange,
            valueRange = 30f..150f,
            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary)
        )
        Text(
            "${targetWeight.toInt()} kg",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        // Card "Challenging goal!"
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "CHALLENGING GOAL!",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isLosing) {
                        "You're looking to lose ${
                            String.format("%.1f", diffPercentage)
                        }% of your body weight. This will significantly improve your heart health and energy levels."
                    } else {
                        "Gaining ${
                            String.format("%.1f", diffPercentage)
                        }% body weight requires consistent surplus. This will help build strength and metabolic resilience."
                    },
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun StepLayout(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.padding(horizontal = 24.dp)) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text(
            subtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(40.dp))
        content()
    }
}

@Composable
fun PaddingBox(content: @Composable () -> Unit) {
    Box(Modifier.padding(horizontal = 24.dp)) { content() }
}

@Preview(showBackground = true)
@Composable
fun GoalStepPreview() {
    FitflowTheme {
        GoalStep (selectedGoal = FitnessGoal.WEIGHT_LOSS, onGoalSelected = {})
    }
}


@Preview(showBackground = true)
@Composable
fun OnboardingHeaderPreview() {
    FitflowTheme {
        OnboardingHeader(
            currentStep = 1,
            totalSteps = 4
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BirthYearStepPreview() {
    FitflowTheme {
        BirthYearStep(birthYear = 2000, onYearChange = {})
    }
}

@Preview(showBackground = true)
@Composable
fun HeightStepPreview() {
    FitflowTheme {
        HeightStep(height = 170f, onHeightChange = {})
    }
}

@Preview(showBackground = true)
@Composable
fun WeightStepPreview() {
    FitflowTheme {
        WeightStep(
            weight = 65f,
            height = 170f,
            goal = FitnessGoal.WEIGHT_LOSS,
            onWeightChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TargetWeightStepPreview() {
    FitflowTheme {
        TargetWeightStep(targetWeight = 60f, currentWeight = 47f, onTargetChange = {})
    }
}
