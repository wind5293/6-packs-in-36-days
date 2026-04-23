package com.example.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.ui.theme.*

@Composable
fun ProfileScreen(onReCalibrate: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("IDENTITY", color = White40, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Row {
                    Text("SUBJECT ", color = TextDim, fontSize = 28.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                    Text("ZERO", color = AccentNeon, fontSize = 28.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                }
            }
            IconButton(
                onClick = {},
                modifier = Modifier.background(White05, RoundedCornerShape(16.dp)).border(1.dp, White05, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = White40)
            }
        }

        // Stats Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, White05, RoundedCornerShape(32.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatsItem("WEIGHT", "65", "kg", false)
                StatsItem("HEIGHT", "170", "cm", false)
                StatsItem("BMI", "22.5", "", true)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("OPERATIONS", color = White40, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Action Button
        Card(
            colors = CardDefaults.cardColors(containerColor = White05),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, White05, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .clickable { onReCalibrate() }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(AccentNeon.copy(alpha=0.1f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = AccentNeon)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("RE-CALIBRATE BODY STATS", color = TextDim, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text("REVIEW YOUR ONBOARDING SETUP", color = White40, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = White40)
            }
        }
    }
}

@Composable
fun StatsItem(label: String, value: String, unit: String, isHighlight: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = White40, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = if (isHighlight) AccentNeon else TextDim, fontSize = 24.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
            if (unit.isNotEmpty()) {
                Text(unit, color = White40, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp, start = 2.dp))
            }
        }
    }
}
