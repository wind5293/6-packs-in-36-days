package com.example.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fitflow.FitFlowApplication
import com.example.fitflow.data.model.Exercise
import com.example.fitflow.data.model.WorkoutExercise
import com.example.fitflow.ui.theme.CardBackground
import com.example.fitflow.ui.theme.CardNested
import com.example.fitflow.ui.theme.OrangeGlow
import com.example.fitflow.ui.theme.OrangePrimary
import com.example.fitflow.utils.GifUrlHelper
import com.example.fitflow.viewmodel.WorkoutPlannerViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlanScreen(
    dayNumber: Int,
    dayTitle: String,
    initialExercises: List<WorkoutExercise>,
    allDatabaseExercises: List<Exercise>,
    plannerViewModel: WorkoutPlannerViewModel,
    onBack: () -> Unit,
    onSave: (List<WorkoutExercise>) -> Unit
) {
    // Kích hoạt chế độ chỉnh sửa khi mở màn hình
    LaunchedEffect(initialExercises) {
        plannerViewModel.enterEditMode(initialExercises)
    }

    val editablePlan by plannerViewModel.editablePlan.collectAsState()
    val isEditMode by plannerViewModel.isEditMode.collectAsState()
    val savedExercises by plannerViewModel.savedExercises.collectAsState()

    // Khi người dùng bấm lưu trong ViewModel, savedExercises sẽ phát ra danh sách mới
    LaunchedEffect(savedExercises) {
        savedExercises?.let {
            onSave(it)
            plannerViewModel.consumeSavedExercises()
        }
    }

    var showReplaceDialogForIndex by remember { mutableStateOf<Int?>(null) }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragAccumulatedPx by remember { mutableFloatStateOf(0f) }
    var draggedItemOffsetPx by remember { mutableFloatStateOf(0f) }
    val dragStepPx = with(LocalDensity.current) { 72.dp.toPx() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "EDIT PLAN",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Day $dayNumber • $dayTitle",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        plannerViewModel.exitEditMode()
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { plannerViewModel.saveChanges() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "SAVE",
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (editablePlan.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(editablePlan) { index, exercise ->
                        EditExerciseRowItem(
                            index = index,
                            exercise = exercise,
                            isDragging = draggingIndex == index,
                            onDragStart = {
                                draggingIndex = index
                                dragAccumulatedPx = 0f
                                draggedItemOffsetPx = 0f
                            },
                            onDrag = { deltaY ->
                                val currentIndex = draggingIndex ?: return@EditExerciseRowItem
                                dragAccumulatedPx += deltaY
                                draggedItemOffsetPx += deltaY

                                if (dragAccumulatedPx > dragStepPx && currentIndex < editablePlan.lastIndex) {
                                    plannerViewModel.moveExercise(currentIndex, currentIndex + 1)
                                    draggingIndex = currentIndex + 1
                                    dragAccumulatedPx -= dragStepPx
                                    draggedItemOffsetPx -= dragStepPx
                                } else if (dragAccumulatedPx < -dragStepPx && currentIndex > 0) {
                                    plannerViewModel.moveExercise(currentIndex, currentIndex - 1)
                                    draggingIndex = currentIndex - 1
                                    dragAccumulatedPx += dragStepPx
                                    draggedItemOffsetPx += dragStepPx
                                }
                            },
                            onDragEnd = {
                                draggingIndex = null
                                dragAccumulatedPx = 0f
                                draggedItemOffsetPx = 0f
                            },
                            dragOffsetPx = if (draggingIndex == index) draggedItemOffsetPx else 0f,
                            onAdjustQty = { delta -> plannerViewModel.adjustQuantity(index, delta) },
                            onReplace = { showReplaceDialogForIndex = index }
                        )
                    }
                }
            }

            // Replace Exercise Dialog
            showReplaceDialogForIndex?.let { index ->
                val exerciseToReplace = editablePlan[index]
                ReplaceExerciseDialog(
                    exerciseName = exerciseToReplace.name,
                    category = exerciseToReplace.category,
                    allDatabaseExercises = allDatabaseExercises,
                    onDismiss = { showReplaceDialogForIndex = null },
                    onSelect = { selectedDbExercise ->
                        val gifFile = selectedDbExercise.local_gifs.firstOrNull() ?: ""
                        val newEx = WorkoutExercise(
                            category = selectedDbExercise.exercise_type,
                            name = selectedDbExercise.name,
                            sets = exerciseToReplace.sets,
                            reps = if (selectedDbExercise.log_type.lowercase() == "reps") 12 else 0,
                            kcal = 45,
                            durationSec = if (selectedDbExercise.log_type.lowercase() != "reps") 60 else 0,
                            gifFileName = gifFile,
                            description = selectedDbExercise.instructions.joinToString("\n")
                        )
                        plannerViewModel.replaceExercise(index, newEx)
                        showReplaceDialogForIndex = null
                    }
                )
            }
        }
    }
}

@Composable
private fun EditExerciseRowItem(
    index: Int,
    exercise: WorkoutExercise,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    dragOffsetPx: Float,
    onAdjustQty: (Int) -> Unit,
    onReplace: () -> Unit
) {
    val context = LocalContext.current
    val gifUrl = remember(exercise.gifFileName) {
        exercise.gifFileName.takeIf { it.isNotEmpty() }?.let { GifUrlHelper.getUrl(it) }
    }

    val accentColor = OrangePrimary

    val qtyLabel = if (exercise.durationSec > 0) {
        val m = exercise.durationSec / 60
        val s = exercise.durationSec % 60
        String.format("%02d:%02d", m, s)
    } else {
        "x ${exercise.reps}"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(x = 0, y = dragOffsetPx.roundToInt()) }
            .zIndex(if (isDragging) 1f else 0f)
            .border(
                width = if (isDragging) 1.dp else 0.dp,
                color = if (isDragging) OrangePrimary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle for reordering
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDragging) OrangePrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                    .pointerInput(index) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart() },
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragEnd,
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.y)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Drag to reorder",
                    tint = OrangePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            // Thumbnail
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (gifUrl != null) {
                    val imageLoader = (context.applicationContext as FitFlowApplication).imageLoader
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(gifUrl)
                            .crossfade(true)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = exercise.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = accentColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Exercise Title and adjust
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.category.uppercase(),
                    color = accentColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = exercise.name.uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 18.sp,
                    maxLines = 2
                )
                Spacer(Modifier.height(8.dp))

                // Adjustment Row (- / + reps or duration)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onAdjustQty(-1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = qtyLabel,
                        style = MaterialTheme.typography.titleLarge,
                        color = accentColor,
                        fontSize = 15.sp,
                        modifier = Modifier.widthIn(min = 40.dp),
                        onTextLayout = {}
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onAdjustQty(1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Replace control (Refresh Button)
            IconButton(
                onClick = onReplace,
                modifier = Modifier
                    .size(36.dp)
                    .background(OrangePrimary.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Replace",
                    tint = OrangePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ReplaceExerciseDialog(
    exerciseName: String,
    category: String,
    allDatabaseExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onSelect: (Exercise) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, allDatabaseExercises) {
        allDatabaseExercises.filter { dbEx ->
            dbEx.name.contains(searchQuery, ignoreCase = true) ||
            dbEx.exercise_type.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBackground,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REPLACE EXERCISE",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 20.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                Text(
                    text = "Replacing: $exerciseName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search exercises...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = OrangePrimary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        cursorColor = OrangePrimary
                    )
                )

                // Exercise List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered.size) { index ->
                        val dbEx = filtered[index]
                        val matchesCategory = dbEx.exercise_type.lowercase() == category.lowercase()
                        val itemAccentColor = if (matchesCategory) OrangePrimary else Color.Gray

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (matchesCategory) OrangeGlow else CardNested,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(dbEx) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(itemAccentColor.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.FitnessCenter,
                                        null,
                                        tint = itemAccentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dbEx.exercise_type.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = itemAccentColor,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = dbEx.name.uppercase(),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = dbEx.difficulty.capitalize() + " · " + dbEx.equipment.joinToString(", ").capitalize(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
