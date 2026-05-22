package com.example.fitflow.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.fitflow.data.model.FitnessGoal
import com.example.fitflow.notification.WorkoutReminderReceiver
import com.example.fitflow.ui.theme.FitflowTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun OnboardingScreen(
    onComplete: (
        selectedGoal: FitnessGoal,
        height: Float,
        weight: Float,
        birthYear: Int,
        targetWeight: Float,
        workoutTime: String
            ) -> Unit
) {
    val totalSteps = 4
    val pagerState = rememberPagerState(pageCount = { totalSteps })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // Lấy context để gửi Broadcast

    // State chung
    val selectedGoal = FitnessGoal.WEIGHT_LOSS
    var height by remember { mutableFloatStateOf(170f) }
    var weight by remember { mutableFloatStateOf(65f) }
    var birthYear by remember { mutableIntStateOf(2000) }
    var targetWeight by remember { mutableFloatStateOf(60f) }
    val workoutTime = "08:00"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 32.dp, bottom = 24.dp)
    ) {
        // --- HEADER & PROGRESS ---
        OnboardingHeader(currentStep = pagerState.currentPage, totalSteps = totalSteps)

        // --- PAGER CONTENT ---
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) { page ->
            when (page) {
                0 -> HeightStep(height) { height = it }
                1 -> BirthYearStep(birthYear) { birthYear = it }
                2 -> TargetWeightStep(targetWeight, weight) { targetWeight = it }
                3 -> WeightStep(weight, height, selectedGoal, { weight = it })
            }
        }

        // --- BOTTOM BUTTON ---
        PaddingBox {
            Button(
                onClick = {
                    if (pagerState.currentPage < totalSteps - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        val testIntent = Intent(context, WorkoutReminderReceiver::class.java)
                        context.sendBroadcast(testIntent)

                        onComplete(
                            selectedGoal,
                            height,
                            weight,
                            birthYear,
                            targetWeight,
                            workoutTime
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    if (pagerState.currentPage == totalSteps - 1) "ACTIVATE JOURNEY" else "CONTINUE",
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
        YearWheelPicker(
            selectedYear = birthYear,
            years = (1950..2024).toList(),
            onYearChange = onYearChange
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
    var useCm by remember { mutableStateOf(true) }
    val cmValue = height.toInt().coerceIn(120, 220)
    val inchValue = (cmValue / 2.54f).toInt()
    val feet = inchValue / 12
    val inches = inchValue % 12

    StepLayout(title = "HOW TALL ARE YOU?", subtitle = "HEIGHT (CM)") {
        UnitToggle(
            left = "CM",
            right = "FT",
            isLeftSelected = useCm,
            onToggle = { useCm = it }
        )
        Spacer(Modifier.height(20.dp))
        RulerPicker(
            selectedValue = cmValue,
            values = (120..220).toList(),
            majorTickEvery = 5,
            onSelect = { onHeightChange(it.toFloat()) }
        )
        Spacer(Modifier.height(18.dp))
        Text(
            if (useCm) "${cmValue} cm" else "$feet ft $inches in",
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
    var useKg by remember { mutableStateOf(true) }
    val kgValue = weight.coerceIn(30f, 150f)
    val lbsValue = kgValue * 2.20462f

    StepLayout(title = "WHAT'S YOUR WEIGHT?", subtitle = "CURRENT WEIGHT (KG)") {
        UnitToggle(
            left = "KG",
            right = "LB",
            isLeftSelected = useKg,
            onToggle = { useKg = it }
        )
        Spacer(Modifier.height(20.dp))
        RulerPicker(
            selectedValue = kgValue.toInt(),
            values = (30..150).toList(),
            majorTickEvery = 5,
            onSelect = { onWeightChange(it.toFloat()) }
        )
        Spacer(Modifier.height(18.dp))
        Text(
            if (useKg) "${String.format("%.1f", kgValue)} kg" else "${String.format("%.1f", lbsValue)} lb",
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
    var useKg by remember { mutableStateOf(true) }
    val kgValue = targetWeight.coerceIn(30f, 150f)
    val lbsValue = kgValue * 2.20462f

    StepLayout(title = "GOAL WEIGHT?", subtitle = "TARGET WEIGHT (KG)") {
        UnitToggle(
            left = "KG",
            right = "LB",
            isLeftSelected = useKg,
            onToggle = { useKg = it }
        )
        Spacer(Modifier.height(20.dp))
        RulerPicker(
            selectedValue = kgValue.toInt(),
            values = (30..150).toList(),
            majorTickEvery = 5,
            onSelect = { onTargetChange(it.toFloat()) },
            majorTickColor = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(18.dp))
        Text(
            if (useKg) "${kgValue.toInt()} kg" else "${String.format("%.1f", lbsValue)} lb",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTimeStep(workoutTime: String, onTimeSelected: (String) -> Unit) {
    val timePickerState = rememberTimePickerState(
        initialHour = 0,
        initialMinute = 0,
        is24Hour = true
    )

    StepLayout(title = "SET YOUR REMINDER", subtitle = "WORKOUT TIME") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            TimeInput(
                state = timePickerState,
                modifier = Modifier,
                colors = TimePickerDefaults.colors()
            )
            LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                onTimeSelected("${timePickerState.hour}:${timePickerState.minute}")
            }
            Text(
                "We'll remind you to stay consistent!",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
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

@Preview(showBackground = true)
@Composable
fun WorkoutTimeStepPreview() {
    FitflowTheme {
        WorkoutTimeStep(workoutTime = "08:00", onTimeSelected = {})
    }
}

@Composable
fun UnitToggle(
    left: String,
    right: String,
    isLeftSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        UnitPill(
            text = left,
            selected = isLeftSelected,
            modifier = Modifier.weight(1f),
            onClick = { onToggle(true) }
        )
        UnitPill(
            text = right,
            selected = !isLeftSelected,
            modifier = Modifier.weight(1f),
            onClick = { onToggle(false) }
        )
    }
}

@Composable
fun UnitPill(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun RulerPicker(
    selectedValue: Int,
    values: List<Int>,
    majorTickEvery: Int,
    onSelect: (Int) -> Unit,
    majorTickColor: Color = MaterialTheme.colorScheme.primary
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(values) { value ->
            val isSelected = value == selectedValue
            RulerTick(
                value = value,
                isMajor = value % majorTickEvery == 0,
                isSelected = isSelected,
                majorTickColor = majorTickColor,
                onClick = { onSelect(value) }
            )
        }
    }
}

@Composable
fun RulerTick(
    value: Int,
    isMajor: Boolean,
    isSelected: Boolean,
    majorTickColor: Color,
    onClick: () -> Unit
) {
    val tickHeight: Dp = if (isMajor) 44.dp else 24.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(22.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(if (isSelected) 3.dp else 2.dp)
                .height(tickHeight)
                .background(
                    if (isSelected) majorTickColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                    RoundedCornerShape(2.dp)
                )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (isMajor) value.toString() else "",
            fontSize = 10.sp,
            color = if (isSelected) majorTickColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun YearWheelPicker(
    selectedYear: Int,
    years: List<Int>,
    onYearChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(vertical = 56.dp)
        ) {
            items(years) { year ->
                val isSelected = year == selectedYear
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onYearChange(year) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = year.toString(),
                        fontSize = if (isSelected) 32.sp else 20.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                        fontStyle = if (isSelected) FontStyle.Italic else FontStyle.Normal
                    )
                }
            }
        }
    }
}