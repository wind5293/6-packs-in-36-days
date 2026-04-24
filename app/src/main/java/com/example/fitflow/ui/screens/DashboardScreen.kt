package com.example.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.ui.theme.AccentNeon
import com.example.fitflow.ui.theme.BackgroundDark
import com.example.fitflow.ui.theme.CardDark
import com.example.fitflow.ui.theme.SecondaryBlue
import com.example.fitflow.ui.theme.TextDim
import com.example.fitflow.ui.theme.White05
import com.example.fitflow.ui.theme.White10
import com.example.fitflow.ui.theme.White40


@Composable
fun DashboardScreen() {
    // Mutable state để UI tự động cập nhật khi người dùng bấm nút
    var steps by remember { mutableIntStateOf(0) }
    var water by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        HeaderSection()
        Spacer(modifier = Modifier.height(32.dp))
        StreakSummarySection()
        Spacer(modifier = Modifier.height(32.dp))
        HealthMetricsSection(
            steps = steps,
            onAddSteps = { steps += 500 },
            water = water,
            onAddWater = { water += 250 }
        )
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "STATUS REPORT",
                color = White40,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Row {
                Text(stringResource(
                    R.string.dashboard_first_half_title),
                    color = TextDim,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic
                )
                Text(stringResource(R.string.dashboard_second_half_title),
                    color = AccentNeon,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic
                )
            }
        }
        IconButton(
            onClick = {},
            modifier = Modifier
                .background(
                    White05,
                    RoundedCornerShape(50)
                )
                .border(
                    1.dp,
                    White05,
                    RoundedCornerShape(50)
                )
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notify",
                tint = White40
            )
        }
    }
}

@Composable
fun StreakSummarySection() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .weight(1f)
                .height(160.dp)
                .border(
                    1.dp,
                    AccentNeon.copy(alpha=0.3f),
                    RoundedCornerShape(32.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = AccentNeon.copy(alpha = 0.5f)
                )
                Column {
                    Text(
                        stringResource(R.string.dashboard_0),
                        fontSize = 48.sp,
                        color = AccentNeon,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        stringResource(R.string.dashboard_streak_cycle),
                        fontSize = 10.sp,
                        color = White40,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .weight(1f)
                .height(160.dp)
                .border(
                    1.dp,
                    SecondaryBlue.copy(alpha=0.3f),
                    RoundedCornerShape(32.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = SecondaryBlue.copy(alpha = 0.5f)
                )
                Column {
                    Text(
                        stringResource(R.string.dashboard_weight_loss),
                        fontSize = 18.sp,
                        color = SecondaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        stringResource(R.string.dashboard_phase_01),
                        fontSize = 10.sp,
                        color = White40,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HealthMetricsSection(
    steps: Int, onAddSteps: () -> Unit,
    water: Int, onAddWater: () -> Unit
) {
    Text(
        stringResource(R.string.dashboard_health_metrics),
        fontSize = 11.sp,
        color = White40,
        fontWeight = FontWeight.Bold,
        letterSpacing = 3.sp
    )
    Spacer(modifier = Modifier.height(12.dp))

    MetricCard(
        stringResource(R.string.dashboard_steps),
        steps.toString(),
        10000,
        stringResource(R.string.dashboard_unit_steps),
        AccentNeon,
        onAddSteps
    )
    Spacer(modifier = Modifier.height(12.dp))
    MetricCard(
        stringResource(R.string.dashboard_water),
        water.toString(),
        2500,
        stringResource(R.string.dashboard_unit_ml),
        SecondaryBlue,
        onAddWater
    )
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    goal: Int,
    unit: String,
    mainColor: Color,
    onClick: () -> Unit
) {
    val progress = (value.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(
                1.dp,
                White05,
                RoundedCornerShape(24.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        mainColor.copy(alpha=0.1f),
                        RoundedCornerShape(12.dp)
                    )
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        label,
                        color = White40,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "$value / $goal $unit",
                        color = TextDim,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            White05,
                            RoundedCornerShape(50)
                        )
                ) {
                    if (progress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(4.dp)
                                .background(
                                    mainColor,
                                    RoundedCornerShape(50)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
            // Clickable Add Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        White10,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = TextDim
                )
            }
        }
    }
}