package com.example.fitflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.data.ExerciseRepository
import com.example.fitflow.ui.theme.FitflowTheme

@Composable
fun LibraryScreen() {
    val selectedCategory = remember { mutableStateOf("ALL") }
    val categories = remember { listOf("ALL") + ExerciseRepository.getCategoryList() }
    val filteredExercises = ExerciseRepository.getExercisesByCategory(selectedCategory.value)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "KNOWLEDGE",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Row {
                    Text(
                        "LIBRARY",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
            IconButton(
                onClick = {},
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(16.dp)
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Category Filter
        LazyColumn(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory.value == category,
                            enabled = true,
                            onClick = { selectedCategory.value = category },
                            label = {
                                Text(
                                    category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }
        }

        // Exercises List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredExercises) { exercise ->
                ExerciseCard(
                    category = exercise.category,
                    name = exercise.name,
                    description = getExerciseDescription(exercise.name, exercise.category),
                    reps = "${exercise.reps} REPS",
                    sets = "${exercise.sets} SETS",
                    cals = "${exercise.kcal} KCAL"
                )
            }
        }
    }
}

/**
 * Generate description for exercise based on name and category
 */
fun getExerciseDescription(name: String, category: String): String {
    return when {
        name.contains("Push", ignoreCase = true) -> "A fundamental strength exercise that targets your chest, shoulders, and triceps."
        name.contains("Squat", ignoreCase = true) -> "Builds leg strength and improves lower body stability."
        name.contains("Lunge", ignoreCase = true) -> "Enhances leg strength and balance. Great for glute and quads activation."
        name.contains("Plank", ignoreCase = true) -> "Core strengthening exercise that improves stability and posture."
        name.contains("Dip", ignoreCase = true) -> "Advanced upper body exercise for chest, shoulders, and triceps."
        name.contains("Burpee", ignoreCase = true) -> "Full-body cardiovascular exercise combining cardio and strength training."
        name.contains("Jump", ignoreCase = true) -> "Plyometric exercise that builds explosive power and cardiovascular endurance."
        name.contains("Climbing", ignoreCase = true) -> "Dynamic core and cardio exercise that engages multiple muscle groups."
        name.contains("Sprint", ignoreCase = true) -> "High-intensity cardio exercise for explosive speed and power."
        name.contains("Rope", ignoreCase = true) -> "Classic cardio exercise that improves coordination and cardiovascular fitness."
        name.contains("Skater", ignoreCase = true) -> "Lateral movement cardio exercise for agility and leg strength."
        name.contains("Star", ignoreCase = true) -> "Full-body cardio exercise combining jumping and lateral movements."
        name.contains("Bridge", ignoreCase = true) -> "Glute and core activation exercise that improves posterior chain strength."
        name.contains("Sit", ignoreCase = true) -> "Isometric leg exercise targeting quadriceps endurance."
        category == "Cardio" -> "Cardio exercise designed to improve cardiovascular endurance and burn calories."
        category == "Strength" -> "Strength training exercise to build muscle and increase power."
        category == "Endurance" -> "Endurance training combining strength and cardio elements."
        else -> "Full-body exercise for overall fitness and conditioning."
    }
}

@Composable
fun ExerciseCard(
    category: String,
    name: String,
    description: String,
    reps: String,
    sets: String,
    cals: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = category,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(50)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                name,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                description,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(reps)
                Chip(sets)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    cals,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun Chip(text: String) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {
    FitflowTheme { LibraryScreen() }
}
