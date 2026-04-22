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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp)
            .padding(top = 40.dp)
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("MANIFEST GENERATION", color = White40, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
            Row {
                Text("WORKOUT ", color = TextDim, fontSize = 32.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                Text("SETUP", color = AccentNeon, fontSize = 32.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Placeholder cho các phần tiếp theo
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("SETUP CONTENT LOADING...", color = White20, fontSize = 10.sp)
        }
    }
}