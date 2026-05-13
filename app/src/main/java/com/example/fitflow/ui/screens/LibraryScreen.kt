package com.example.fitflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
    val searchQuery = remember { mutableStateOf("") }
    val selectedDifficulty = remember { mutableStateOf("ALL") }
    val selectedMuscleGroup = remember { mutableStateOf("ALL") }
    val minCalories = remember { mutableStateOf(25f) }
    val maxCalories = remember { mutableStateOf(80f) }

    val categories = remember { listOf("ALL") + ExerciseRepository.getCategoryList() }
    val difficulties = remember { ExerciseRepository.getDifficultiesList() }
    val muscleGroups = remember { listOf("ALL") + ExerciseRepository.getMuscleGroupsList() }

    val filteredExercises = ExerciseRepository.filterExercises(
        category = selectedCategory.value,
        searchQuery = searchQuery.value,
        minCalories = minCalories.value.toInt(),
        maxCalories = maxCalories.value.toInt(),
        difficulty = selectedDifficulty.value,
        muscleGroup = selectedMuscleGroup.value
    )

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
                .padding(top = 16.dp, bottom = 20.dp),
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

        // Search TextField
        TextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(bottom = 16.dp),
            placeholder = {
                Text(
                    "Find exercise...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.value.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery.value = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 14.sp)
        )

        // Filters in LazyColumn for scrolling
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Category Filter
            item {
                Column {
                    Text(
                        "CATEGORY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
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

            // Difficulty Filter
            item {
                Column {
                    Text(
                        "DIFFICULTY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ALL option
                        FilterChip(
                            selected = selectedDifficulty.value == "ALL",
                            enabled = true,
                            onClick = { selectedDifficulty.value = "ALL" },
                            label = {
                                Text(
                                    "ALL",
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
                        difficulties.forEach { difficulty ->
                            FilterChip(
                                selected = selectedDifficulty.value == difficulty,
                                enabled = true,
                                onClick = { selectedDifficulty.value = difficulty },
                                label = {
                                    Text(
                                        difficulty,
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

            // Calorie Range Slider
            item {
                Column {
                    Text(
                        "CALORIES: ${minCalories.value.toInt()} — ${maxCalories.value.toInt()} KCAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    RangeSlider(
                        value = minCalories.value..maxCalories.value,
                        onValueChange = { range ->
                            minCalories.value = range.start
                            maxCalories.value = range.endInclusive
                        },
                        valueRange = 25f..80f,
                        steps = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                        )
                    )
                }
            }

            // Muscle Group Filter
            item {
                Column {
                    Text(
                        "MUSCLE GROUP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        muscleGroups.forEach { group ->
                            FilterChip(
                                selected = selectedMuscleGroup.value == group,
                                enabled = true,
                                onClick = { selectedMuscleGroup.value = group },
                                label = {
                                    Text(
                                        group,
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

            // Results count
            item {
                Text(
                    "${filteredExercises.size} exercises found",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Exercises List
            items(filteredExercises) { exercise ->
                ExerciseCard(
                    category = exercise.category,
                    name = exercise.name,
                    description = getExerciseDescription(exercise.name, exercise.category),
                    reps = "${exercise.reps} REPS",
                    sets = "${exercise.sets} SETS",
                    cals = "${exercise.kcal} KCAL",
                    difficulty = exercise.difficulty,
                    muscleGroups = exercise.muscleGroups.take(2) // Show max 2 muscle groups
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
    cals: String,
    difficulty: String = "MEDIUM",
    muscleGroups: List<String> = emptyList()
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
            // Top row: Category + Difficulty badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                // Difficulty badge with color
                val difficultyColor = when (difficulty) {
                    "EASY" -> MaterialTheme.colorScheme.secondary
                    "HARD" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                }
                Text(
                    text = difficulty,
                    color = difficultyColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .background(
                            difficultyColor.copy(alpha = 0.1f),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.weight(1f))
            }

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

            // Muscle groups
            if (muscleGroups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    muscleGroups.forEach { group ->
                        Text(
                            group,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

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
