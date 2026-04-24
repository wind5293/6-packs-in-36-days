package com.example.fitflow.ui.screens

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
import com.example.fitflow.ui.theme.FitflowTheme

@Composable
fun WorkoutSetupScreen(onComplete: () -> Unit) {
    var selectedEquipment by remember { mutableStateOf("bodyweight") }
    var selectedFocus by remember { mutableStateOf(setOf("Full Body")) }
    var daysPerWeek by remember { mutableFloatStateOf(5f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .padding(top = 40.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("MANIFEST GENERATION", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
            Row {
                Text("WORKOUT ", color = MaterialTheme.colorScheme.onBackground, fontSize = 32.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                Text("SETUP",   color = MaterialTheme.colorScheme.primary,       fontSize = 32.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text("EQUIPMENT LEVEL", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EquipmentItem("Bodyweight Only", "No equipment required",       selectedEquipment == "bodyweight") { selectedEquipment = "bodyweight" }
            EquipmentItem("Minimalist",      "Dumbbells & Resistance bands", selectedEquipment == "minimal")   { selectedEquipment = "minimal" }
            EquipmentItem("Full Protocol",   "Complete high-end gym access", selectedEquipment == "gym")       { selectedEquipment = "gym" }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text("FREQUENCY", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("${daysPerWeek.toInt()} DAYS / WEEK", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
        }
        Slider(
            value = daysPerWeek,
            onValueChange = { daysPerWeek = it },
            valueRange = 1f..7f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onComplete,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("FINALIZE PROTOCOL", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun EquipmentItem(title: String, desc: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor    = if (isSelected) MaterialTheme.colorScheme.primary                             else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
    val textColor  = if (isSelected) MaterialTheme.colorScheme.onPrimary                           else MaterialTheme.colorScheme.onBackground
    val descColor  = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)

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
            Text(desc.uppercase(),  color = descColor, fontSize = 9.sp,  fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutSetupScreenPreview() {
    FitflowTheme { WorkoutSetupScreen({}) }
}
