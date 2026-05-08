package com.example.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    workoutPlan: List<DayPlan>,
    completedDays: Set<Int> = emptySet(),
    currentDay: Int = -1,
    onDayClick: (Int) -> Unit = {}
) {
    val groupedByWeek = workoutPlan.groupBy { (it.day - 1) / 7 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("MASTER MANIFEST", color = White40, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Row {
                    Text("MONTHLY ", color = TextDim, fontSize = 28.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                    Text("TIMELINE", color = AccentNeon, fontSize = 28.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                }
            }
            IconButton(
                onClick = {},
                modifier = Modifier.background(White05, RoundedCornerShape(16.dp)).border(1.dp, White05, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = White40)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Duyệt qua từng tuần
            groupedByWeek.forEach { (weekIndex, daysInWeek) ->
                val weekNum = weekIndex + 1

                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "WEEK ${weekNum.toString().padStart(2, '0')}",
                            color = AccentNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontStyle = FontStyle.Italic
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(modifier = Modifier.height(1.dp).weight(1f).background(White10))
                    }
                }

                // Duyệt qua danh sách DayPlan thực tế của tuần đó
                items(daysInWeek) { dayPlan ->
                    DayPlanItem(
                        dayPlan = dayPlan,
                        isCurrentDay = dayPlan.dayNumber == currentDay,
                        isCompleted = dayPlan.dayNumber in completedDays,
                        onClick = { onDayClick(dayPlan.dayNumber) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Regenerate Logic Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = AccentNeon),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {}
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("NEW CYCLE", color = BackgroundDark, fontSize = 20.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                            Text("RE-GENERATE ENTIRE LOGIC", color = BackgroundDark.copy(alpha=0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DayPlanItem(
    dayPlan: DayPlan,
    isCurrentDay: Boolean = false,
    isCompleted: Boolean = false,
    onClick: () -> Unit = {}
) {
    val dayNum = dayPlan.day
    val isRest = dayPlan.isRest

    val cardAlpha   = if (isCompleted) 0.4f else 1f
    val borderColor = when {
        isCurrentDay -> AccentNeon
        isCompleted  -> White05.copy(alpha = 0.3f)
        else         -> White05
    }
    val cardBg      = when {
        isCurrentDay -> AccentNeon.copy(alpha = 0.05f)
        else         -> CardDark
    }
    val badgeBg     = when {
        isCurrentDay -> AccentNeon
        isCompleted  -> White05.copy(alpha = 0.5f)
        else         -> White05
    }
    val badgeText   = when {
        isCurrentDay -> BackgroundDark
        else         -> White40
    }
    val labelColor  = when {
        isCurrentDay -> AccentNeon
        isCompleted  -> White40.copy(alpha = 0.5f)
        else         -> White20
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(cardAlpha)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(badgeBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$dayNum", color = badgeText, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("DAY $dayNum", color = labelColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text(
                            if (isRest) "REST & RECOVERY" else "SCHEDULED ACTIVITY",
                            color = if (isCompleted) TextDim.copy(alpha = 0.5f) else TextDim,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            if (!isRest) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dayPlan.exercises.take(3).forEach { exercise ->
                        ExerciseTag(exercise.name)
                    }
                    if (dayPlan.exercises.size > 3) {
                        ExerciseTag("+${dayPlan.exercises.size - 3}")
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseTag(name: String) {
    Box(
        modifier = Modifier
            .background(White05, RoundedCornerShape(8.dp))
            .border(1.dp, White05, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(name.uppercase(), color = White40, fontSize = 8.sp, fontWeight = FontWeight.Black)
    }
}

val Transparent = Color(0x00000000)

@Preview(showBackground = true)
@Composable
fun PlannerScreenPreview() {
    // Pass emptyList để sửa lỗi không truyền tham số cho Preview
    FitflowTheme { PlannerScreen(emptyList()) }
}