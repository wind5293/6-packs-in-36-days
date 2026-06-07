package com.example.fitflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fitflow.FitFlowApplication
//import com.example.fitflow.data.model.Exercise
import com.example.fitflow.ui.theme.FitflowTheme
import com.example.fitflow.utils.GifUrlHelper
import com.example.fitflow.viewmodel.LibraryViewModel
import androidx.compose.foundation.clickable
import com.example.fitflow.data.model.Exercise

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val filterState by viewModel.filterState.collectAsState()
    val filteredExercises by viewModel.filteredExercises.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val difficulties by viewModel.difficulties.collectAsState()
    val muscleGroups by viewModel.muscleGroups.collectAsState()

    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

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
                            fontStyle = FontStyle.Normal
                        )
                    }
                }
            }
//            IconButton(
//                onClick = {},
//                modifier = Modifier.background(
//                    MaterialTheme.colorScheme.primary,
//                    RoundedCornerShape(16.dp)
//                )
//            ) {
//                Icon(
//                    Icons.Default.Add,
//                    contentDescription = "Add",
//                    tint = MaterialTheme.colorScheme.onPrimary
//                )
//            }
        }

        // Search TextField
        TextField(
            value = filterState.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
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
                if (filterState.searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.setSearchQuery("") },
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
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
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
                FilterSection(title = "CATEGORY") {
                    categories.forEach { category ->
                        LibraryFilterChip(
                            label = category,
                            selected = filterState.category == category,
                            onClick = { viewModel.setCategory(category) }
                        )
                    }
                }
            }

            // Difficulty Filter
            item {
                FilterSection(title = "DIFFICULTY") {
                    difficulties.forEach { difficulty ->
                        LibraryFilterChip(
                            label = difficulty,
                            selected = filterState.difficulty == difficulty,
                            onClick = { viewModel.setDifficulty(difficulty) }
                        )
                    }
                }
            }

            // Muscle Group Filter
            item {
                FilterSection(title = "MUSCLE GROUP") {
                    muscleGroups.forEach { group ->
                        LibraryFilterChip(
                            label = group,
                            selected = filterState.muscleGroup == group,
                            onClick = { viewModel.setMuscleGroup(group) }
                        )
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

            items(filteredExercises) { exercise ->
                ExerciseListItem(
                    exercise = exercise,
                    onClick = { selectedExercise = exercise }
                )
            }
        }
    }

    if (selectedExercise != null) {
        LibraryExerciseInstructionOverlayScreen(
            exercise = selectedExercise!!,
            onClose = { selectedExercise = null }
        )
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable RowScope.() -> Unit
) {
    Column {
        Text(
            title,
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
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun LibraryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        enabled = true,
        onClick = onClick,
        label = {
            Text(
                label,
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
        modifier = Modifier.height(32.dp)
    )
}

@Composable
fun ExerciseListItem(
    exercise: Exercise,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val imageLoader = (context.applicationContext as FitFlowApplication).imageLoader
    
    val badgeColor = when (exercise.difficulty) {
        "beginner" -> MaterialTheme.colorScheme.secondary
        "advanced" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Media Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                if (exercise.local_gifs.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(GifUrlHelper.getUrl(exercise.local_gifs.first()))
                            .crossfade(true)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = exercise.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            exercise.exercise_type.take(3).uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
                
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )

                // Difficulty badge on top-right
                Text(
                    text = exercise.difficulty.replaceFirstChar { it.uppercase() },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = badgeColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Info Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = exercise.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                val muscles = exercise.target_muscles
                    .filter { it != "Main" }
                    .take(3)
                    .joinToString(" · ")
                Text(
                    text = muscles.ifBlank { exercise.exercise_type },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
        }
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
                fontStyle = FontStyle.Normal
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
    FitflowTheme {
        LibraryScreen()
    }
}
