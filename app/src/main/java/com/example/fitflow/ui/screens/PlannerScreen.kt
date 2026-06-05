package com.example.fitflow.ui.screens

import androidx.activity.ComponentActivity
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.ExperimentalFoundationApi
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.ui.theme.FitflowTheme
import com.example.fitflow.viewmodel.UserViewModel
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Zap
import com.composables.icons.lucide.Coffee

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    workoutPlan: List<DayPlan>,
    completedDays: Set<Int> = emptySet(),
    currentDay: Int = -1, // Ignored, we calculate it ourselves to include rest days
    onDayClick: (Int) -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel(context as ComponentActivity)
    val userProfile by userViewModel.userProfile.collectAsState()

    // Tự tính currentDay thực sự (ngày đầu tiên chưa hoàn thành)
    val realCurrentDay = workoutPlan.firstOrNull { it.dayNumber !in completedDays }?.dayNumber ?: -1

    val daysLeft = (workoutPlan.size - completedDays.size).coerceAtLeast(0)
    val progress = if (workoutPlan.isNotEmpty()) completedDays.size.toFloat() / workoutPlan.size else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Background Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        )
                )

                // Content
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
                ) {
                    // Flash icons & Level
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Lucide.Zap, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Icon(Lucide.Zap, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Icon(Lucide.Zap, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Beginner", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Goal title
                    Text(
                        text = userProfile?.goal?.title ?: "Workout Plan",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                }

                // Overlapping rounded surface at the bottom of the header
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        )
                )
            }
        }

        stickyHeader {
            val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp)
                    .padding(top = topPadding + 16.dp)
            ) {
                // Progress Bar
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$daysLeft",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "days left",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        items(workoutPlan) { dayPlan ->
            val isCompleted = dayPlan.dayNumber in completedDays
            val isCurrent = dayPlan.dayNumber == realCurrentDay

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                DayPlanItemRedesigned(
                    dayPlan = dayPlan,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent,
                    onClick = { onDayClick(dayPlan.dayNumber) },
                    onRestClick = { userViewModel.markDayComplete(dayPlan.dayNumber) }
                )
            }
        }
    }
}

@Composable
fun DayPlanItemRedesigned(
    dayPlan: DayPlan,
    isCompleted: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onRestClick: () -> Unit
) {
    val bgColor = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
    val subTextColor = if (isCurrent) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isCurrent) Color.Transparent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Day ${dayPlan.dayNumber}",
                    color = textColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic
                )
                Text(
                    text = if (isCompleted) "Finished" else if (dayPlan.isRest) "Rest" else "${dayPlan.workoutExercises.size} Exercises",
                    color = subTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isCompleted) {
                // Completed Checkmark
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Finished",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else if (isCurrent) {
                // Current Day Buttons
                if (dayPlan.isRest) {
                    Button(
                        onClick = onRestClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Lucide.Coffee, contentDescription = "Rest", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("REST", fontWeight = FontWeight.Black)
                    }
                } else {
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("START", fontWeight = FontWeight.Black)
                    }
                }
            } else {
                // Upcoming Day
                if (dayPlan.isRest) {
                    Icon(
                        Lucide.Coffee,
                        contentDescription = "Rest",
                        tint = subTextColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}