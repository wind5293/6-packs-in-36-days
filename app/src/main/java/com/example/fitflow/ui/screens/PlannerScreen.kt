package com.example.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // Đã thêm import này
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh // Đã thêm import này
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen() {
    val weeks = (1..4).toList()
    val daysPerWeek = (1..7).toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .padding(bottom = 80.dp) // Space for bottom bar
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
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            weeks.forEach { weekNum ->
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

                items(7) { dayOffset ->
                    val dayNum = (weekNum - 1) * 7 + dayOffset + 1
                    if (dayNum <= 30) {
                        DayPlanItem(dayNum)
                    }
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
fun DayPlanItem(dayNum: Int) {
    val isToday = dayNum == 4 // Giả sử
    val isRest = dayNum % 7 == 0

    Card(
        colors = CardDefaults.cardColors(containerColor = if (isToday) AccentNeon.copy(alpha=0.05f) else CardDark),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, if (isToday) AccentNeon else White05, RoundedCornerShape(24.dp))
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
                            .background(if (isToday) AccentNeon else White05, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$dayNum", color = if (isToday) BackgroundDark else White40, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("DAY $dayNum", color = if (isToday) AccentNeon else White20, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text(
                            if (isRest) "REST & RECOVERY" else "SCHEDULED ACTIVITY",
                            color = TextDim,
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
                    // Mẫu bài tập nhỏ
                    ExerciseTag("Pushup")
                    ExerciseTag("Squat")
                    ExerciseTag("Plank")
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
    FitflowTheme { PlannerScreen() }
}