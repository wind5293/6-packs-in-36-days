package com.example.fitflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.fitflow.R
import com.example.fitflow.data.model.BmiCategory
import com.example.fitflow.data.model.FitnessGoal
import com.example.fitflow.domain.calculateBmi
import com.example.fitflow.domain.getBmiCategory
import com.example.fitflow.ui.theme.FitflowTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.Year
import java.util.Locale
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
    val totalSteps = 6
    val pagerState = rememberPagerState(pageCount = { totalSteps })
    val scope = rememberCoroutineScope()
    // State chung
    val selectedGoal = FitnessGoal.WEIGHT_LOSS
    var height by remember { mutableFloatStateOf(160f) }
    var weight by remember { mutableFloatStateOf(60f) }
    var birthYear by remember { mutableIntStateOf(2000) }
    var targetWeight by remember { mutableFloatStateOf(60f) }
    var workoutTime by remember { mutableStateOf("08:00") }

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
                0 -> WeightStep(weight) { weight = it }
                1 -> HeightStep(height) { height = it }
                2 -> BmiInfoStep(weight = weight, height = height)
                3 -> TargetWeightStep(targetWeight, weight) { targetWeight = it }
                4 -> BirthYearStep(birthYear) { birthYear = it }
                5 -> WorkoutTimeStep(workoutTime = workoutTime) { workoutTime = it }
            }
        }

        // --- BOTTOM BUTTON ---
        PaddingBox {
            Button(
                onClick = {
                    if (pagerState.currentPage < totalSteps - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
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
                    if (pagerState.currentPage == totalSteps - 1)
                        stringResource(R.string.onboarding_action_activate_journey)
                    else
                        stringResource(R.string.onboarding_action_continue),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
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
                stringResource(R.string.onboarding_brand_fit),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                fontStyle = FontStyle.Italic
            )
            Text(
                stringResource(R.string.onboarding_brand_flow),
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
    StepLayout(
        title = stringResource(R.string.onboarding_goal_title),
        subtitle = stringResource(R.string.onboarding_goal_subtitle)
    ) {
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
                            Text(
                                stringResource(goal.titleRes()),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                stringResource(goal.descriptionRes()),
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

private fun FitnessGoal.titleRes(): Int = when (this) {
    FitnessGoal.WEIGHT_LOSS -> R.string.onboarding_goal_weight_loss_title
    FitnessGoal.MUSCLE_GAIN -> R.string.onboarding_goal_muscle_gain_title
    FitnessGoal.ENDURANCE -> R.string.onboarding_goal_endurance_title
    FitnessGoal.MAINTENANCE -> R.string.onboarding_goal_maintenance_title
}

private fun FitnessGoal.descriptionRes(): Int = when (this) {
    FitnessGoal.WEIGHT_LOSS -> R.string.onboarding_goal_weight_loss_desc
    FitnessGoal.MUSCLE_GAIN -> R.string.onboarding_goal_muscle_gain_desc
    FitnessGoal.ENDURANCE -> R.string.onboarding_goal_endurance_desc
    FitnessGoal.MAINTENANCE -> R.string.onboarding_goal_maintenance_desc
}

@Composable
fun BirthYearStep(birthYear: Int, onYearChange: (Int) -> Unit) {
    val currentYear = Year.now().value
    StepLayout(
        title = stringResource(R.string.onboarding_birth_year_title),
        subtitle = stringResource(R.string.onboarding_birth_year_subtitle)
    ) {
        YearWheelPicker(
            selectedYear = birthYear,
            years = (1950..currentYear).toList(),
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

    StepLayout(
        title = stringResource(R.string.onboarding_height_title),
        subtitle = stringResource(R.string.onboarding_height_subtitle)
    ) {
        UnitToggle(
            left = stringResource(R.string.onboarding_unit_cm),
            right = stringResource(R.string.onboarding_unit_ft),
            isLeftSelected = useCm,
            onToggle = { useCm = it }
        )
        Spacer(Modifier.height(20.dp))
        RulerPicker(
            selectedValue = cmValue,
            values = (120..220).toList(),
            majorTickEvery = 5,
            minorTickStep = 1,
            onSelect = { onHeightChange(it.toFloat()) }
        )
        Spacer(Modifier.height(18.dp))
        Text(
            if (useCm) {
                stringResource(R.string.onboarding_height_value_cm_format, cmValue)
            } else {
                stringResource(R.string.onboarding_height_value_ft_in_format, feet, inches)
            },
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
    onWeightChange: (Float) -> Unit
) {
    var useKg by remember { mutableStateOf(true) }
    val kgValue = weight.coerceIn(30f, 150f)
    val lbsValue = kgValue * 2.20462f

    StepLayout(
        title = stringResource(R.string.onboarding_weight_title),
        subtitle = stringResource(R.string.onboarding_weight_subtitle)
    ) {
        UnitToggle(
            left = stringResource(R.string.onboarding_unit_kg),
            right = stringResource(R.string.onboarding_unit_lb),
            isLeftSelected = useKg,
            onToggle = { useKg = it }
        )
        Spacer(Modifier.height(20.dp))
        RulerPicker(
            selectedValue = kgValue.toInt(),
            values = (30..150).toList(),
            majorTickEvery = 5,
            minorTickStep = 1,
            onSelect = { onWeightChange(it.toFloat()) }
        )
        Spacer(Modifier.height(18.dp))
        Text(
            if (useKg) {
                stringResource(R.string.onboarding_weight_value_kg_format, kgValue)
            } else {
                stringResource(R.string.onboarding_weight_value_lb_format, lbsValue)
            },
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BmiInfoStep(weight: Float, height: Float) {
    val bmi = calculateBmi(height = height, weight = weight)
    val category = getBmiCategory(bmi)

    val categoryLabel = when (category) {
        BmiCategory.UNDERWEIGHT -> stringResource(R.string.onboarding_bmi_label_underweight)
        BmiCategory.NORMAL -> stringResource(R.string.onboarding_bmi_label_normal)
        BmiCategory.OVERWEIGHT -> stringResource(R.string.onboarding_bmi_label_overweight)
    }

    val guidance = when (category) {
        BmiCategory.UNDERWEIGHT -> stringResource(R.string.onboarding_bmi_guidance_underweight)
        BmiCategory.NORMAL -> stringResource(R.string.onboarding_bmi_guidance_normal)
        BmiCategory.OVERWEIGHT -> stringResource(R.string.onboarding_bmi_guidance_overweight)
    }

    StepLayout(
        title = stringResource(R.string.onboarding_bmi_title),
        subtitle = stringResource(R.string.onboarding_bmi_subtitle)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%.1f", bmi),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = categoryLabel,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Text(
                text = guidance,
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
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

    StepLayout(
        title = stringResource(R.string.onboarding_target_weight_title),
        subtitle = stringResource(R.string.onboarding_target_weight_subtitle)
    ) {
        UnitToggle(
            left = stringResource(R.string.onboarding_unit_kg),
            right = stringResource(R.string.onboarding_unit_lb),
            isLeftSelected = useKg,
            onToggle = { useKg = it }
        )
        Spacer(Modifier.height(20.dp))
        RulerPicker(
            selectedValue = kgValue.toInt(),
            values = (30..150).toList(),
            majorTickEvery = 5,
            minorTickStep = 1,
            onSelect = { onTargetChange(it.toFloat()) },
            majorTickColor = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(18.dp))
        Text(
            if (useKg) {
                stringResource(R.string.onboarding_target_weight_value_kg_format, kgValue.toInt())
            } else {
                stringResource(R.string.onboarding_weight_value_lb_format, lbsValue)
            },
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
                    stringResource(R.string.onboarding_target_challenge_title),
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isLosing) {
                        stringResource(
                            R.string.onboarding_target_losing_body_format,
                            String.format(Locale.getDefault(), "%.1f", diffPercentage)
                        )
                    } else {
                        stringResource(
                            R.string.onboarding_target_gaining_body_format,
                            String.format(Locale.getDefault(), "%.1f", diffPercentage)
                        )
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
    val initialHour = workoutTime.split(":").getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 8
    val initialMinute = workoutTime.split(":").getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    StepLayout(
        title = stringResource(R.string.onboarding_reminder_title),
        subtitle = stringResource(R.string.onboarding_reminder_subtitle)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.copy(fontWeight = FontWeight.Black)
            ) {
                TimeInput(
                    state = timePickerState,
                    modifier = Modifier,
                    colors = TimePickerDefaults.colors(
                        selectorColor = MaterialTheme.colorScheme.primary,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                onTimeSelected(String.format("%02d:%02d", timePickerState.hour, timePickerState.minute))
            }
            Text(
                stringResource(R.string.onboarding_reminder_helper_text),
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
            onWeightChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BmiInfoStepPreview() {
    FitflowTheme {
        BmiInfoStep(weight = 60f, height = 160f)
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
    minorTickStep: Int,
    onSelect: (Int) -> Unit,
    majorTickColor: Color = MaterialTheme.colorScheme.primary
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val tickSlotWidth = 16.dp
        val tickSpacing = 4.dp
        val horizontalPadding = (maxWidth / 2) - (tickSlotWidth / 2)
        val selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0)

        LaunchedEffect(selectedIndex) {
            if (selectedIndex != listState.firstVisibleItemIndex) {
                listState.scrollToItem(selectedIndex)
            }
        }

        LaunchedEffect(listState, values) {
            snapshotFlow {
                val layoutInfo = listState.layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo
                if (visibleItems.isEmpty()) {
                    null
                } else {
                    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                    visibleItems.minByOrNull { item ->
                        abs((item.offset + item.size / 2) - viewportCenter)
                    }?.index
                }
            }
                .distinctUntilChanged()
                .collect { centeredIndex ->
                if (centeredIndex == null || centeredIndex !in values.indices) return@collect
                val valueAtCenter = values[centeredIndex]
                if (valueAtCenter != selectedValue) {
                    onSelect(valueAtCenter)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            LazyRow(
                state = listState,
                flingBehavior = snapFlingBehavior,
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(tickSpacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(values) { index, value ->
                    val isSelected = value == selectedValue
                    RulerTick(
                        value = value,
                        isMajor = value % majorTickEvery == 0,
                        isMinor = value % minorTickStep == 0,
                        isSelected = isSelected,
                        majorTickColor = majorTickColor,
                        onClick = {
                            scope.launch { listState.animateScrollToItem(index) }
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(majorTickColor)
            )
        }
    }
}

@Composable
fun RulerTick(
    value: Int,
    isMajor: Boolean,
    isMinor: Boolean,
    isSelected: Boolean,
    majorTickColor: Color,
    onClick: () -> Unit
) {
    val tickHeight: Dp = if (isMajor) 40.dp else if (isMinor) 24.dp else 14.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(16.dp)
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
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val clampedSelectedYear = selectedYear.coerceIn(years.first(), years.last())
    val selectedIndex = years.indexOf(clampedSelectedYear).coerceAtLeast(0)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(selectedIndex) {
        listState.scrollToItem(selectedIndex)
    }

    LaunchedEffect(listState, years) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                null
            } else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItems.minByOrNull { item ->
                    abs((item.offset + item.size / 2) - viewportCenter)
                }?.index
            }
        }
            .distinctUntilChanged()
            .collect { centeredIndex ->
            if (centeredIndex == null || centeredIndex !in years.indices) return@collect
            val yearAtCenter = years[centeredIndex]
            if (yearAtCenter != selectedYear) {
                onYearChange(yearAtCenter)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(vertical = 56.dp)
        ) {
            items(years) { year ->
                val isSelected = year == selectedYear
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onYearChange(year)
                            val index = years.indexOf(year)
                            if (index >= 0) {
                                scope.launch { listState.animateScrollToItem(index) }
                            }
                        }
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