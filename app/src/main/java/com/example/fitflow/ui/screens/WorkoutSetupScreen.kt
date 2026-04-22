package com.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.ui.theme.*

@Composable
fun WorkoutSetupScreen(onComplete: () -> Unit) {
    var selectedEquipment by remember { mutableStateOf("bodyweight") }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(24.dp).padding(top = 40.dp)
    ) {
        // Header (Giữ nguyên)
        // ...

        Spacer(modifier = Modifier.height(48.dp))

        // Equipment Selection Section
        Text("EQUIPMENT LEVEL", color = White40, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EquipmentItem("Bodyweight Only", "No equipment required", selectedEquipment == "bodyweight") { selectedEquipment = "bodyweight" }
            EquipmentItem("Minimalist", "Dumbbells & Resistance bands", selectedEquipment == "minimal") { selectedEquipment = "minimal" }
            EquipmentItem("Full Protocol", "Complete high-end gym access", selectedEquipment == "gym") { selectedEquipment = "gym" }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun EquipmentItem(title: String, desc: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) AccentNeon else White05
    val textColor = if (isSelected) BackgroundDark else TextDim
    val descColor = if (isSelected) BackgroundDark.copy(alpha=0.6f) else White20
    val borderColor = if (isSelected) AccentNeon else White10

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column {
            Text(title.uppercase(), color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(desc.uppercase(), color = descColor, fontSize = 9.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
        }
    }
}