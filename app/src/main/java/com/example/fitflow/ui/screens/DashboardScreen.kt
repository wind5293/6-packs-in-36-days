package com.example.fitflow.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.fitflow.data.model.UserProfile
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fitflow.FitFlowApplication
import com.example.fitflow.utils.GifUrlHelper
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.Footprints
import com.composables.icons.lucide.GlassWater
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Lucide
import com.example.fitflow.R
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.DailyHealthMetrics
import com.example.fitflow.data.model.Exercise
import com.example.fitflow.data.model.FitnessGoal
import com.example.fitflow.data.model.StepSource
import com.example.fitflow.data.model.WorkoutLogEntry
import com.example.fitflow.domain.PushYourLimitsCatalog
import com.example.fitflow.ui.components.PushYourLimitsSection
import com.example.fitflow.ui.theme.FitflowTheme
import com.example.fitflow.viewmodel.LibraryFilterState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    completedDays: Set<Int> = emptySet(),
    completedDateMap: Map<LocalDate, Int> = emptyMap(),
    globalWorkoutLogs: List<WorkoutLogEntry> = emptyList(),
    currentStreak: Int = 0,
    longestStreak: Int = 0,
    workoutPlan: List<DayPlan> = emptyList(),
    userProfile: UserProfile? = null,
    startDate: LocalDate? = null,
    healthMetrics: DailyHealthMetrics = DailyHealthMetrics(
        LocalDate.now(),
        0,
        0,
        2000,
        stepGoal = 6000,
        stepSource = StepSource.MANUAL
    ),
    isActivityRecognitionGranted: Boolean = false,
    isStepSensorEnabled: Boolean = false,
    isStepTrackingActive: Boolean = false,
    onUnlockStepSensor: () -> Unit = {},
    onAddWater: (Int) -> Unit = {},
    onSetWaterGoal: (Int) -> Unit = {},
    onSetStepGoal: (Int) -> Unit = {},
    onStartWorkout: () -> Unit = {},
    onOpenChatbot: () -> Unit = {},
    onOpenPlanner: () -> Unit = {},
    onOpenSupplementary: (String) -> Unit = {},
    onOpenDaySummary: (Int, Long) -> Unit = { _, _ -> },
    onOpenHistory: () -> Unit = {},
    onOpenChangeGoal: () -> Unit = {},
    libraryFilterState: LibraryFilterState = LibraryFilterState(),
    libraryExercises: List<Exercise> = emptyList(),
    libraryMuscleGroups: List<String> = listOf("ALL"),
    onLibrarySearchQueryChange: (String) -> Unit = {},
    onLibraryMuscleGroupChange: (String) -> Unit = {},
    onOpenLibrary: () -> Unit = {}
) {
    val totalWorkoutDays = workoutPlan.count { !it.isRest }
    val completedCount = completedDays.size
    val totalKcal = workoutPlan
        .filter { it.dayNumber in completedDays }
        .flatMap { it.workoutExercises }
        .sumOf { it.kcal }
    val currentDayPlan = workoutPlan
        .filter { !it.isRest }
        .firstOrNull { it.dayNumber !in completedDays }

    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = 8.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = paddingValues.calculateBottomPadding() + 16.dp,
        )
    ) {
        stickyHeader {
            HeaderSection(onOpenChatbot = onOpenChatbot)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            WeeklyGoalSection(
                globalWorkoutLogs = globalWorkoutLogs,
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                onViewHistory = onOpenHistory,
                onEditGoal = onOpenChangeGoal,
                onToggleDay = { weekIndex ->
                    val today = LocalDate.now()
                    val todayIndex = if (today.dayOfWeek.value == 7) 0 else today.dayOfWeek.value
                    val startOfWeek = today.minusDays(todayIndex.toLong())
                    val date = startOfWeek.plusDays(weekIndex.toLong())
                    
                    val dayNum = completedDateMap[date] ?: -1
                    onOpenDaySummary(dayNum, date.toEpochDay())
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            if (currentDayPlan != null) {
                DailyChallengeSection(
                    currentDay = currentDayPlan.dayNumber,
                    totalDays = workoutPlan.count { !it.isRest },
                    dayTitle = currentDayPlan.title,
                    exercises = currentDayPlan.workoutExercises.map { it.name },
                    onClick = onOpenPlanner,
                    onOpenSettings = onOpenChangeGoal
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
        item {
            // Push Your Limits horizontal scroller — uses local catalog
            PushYourLimitsSection(
                workouts = PushYourLimitsCatalog.all(),
                onWorkoutClick = onOpenSupplementary,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            DashboardLibrarySection(
                filterState = libraryFilterState,
                filteredExercises = libraryExercises,
                muscleGroups = libraryMuscleGroups,
                onSearchQueryChange = onLibrarySearchQueryChange,
                onMuscleGroupChange = onLibraryMuscleGroupChange,
                onViewAll = onOpenLibrary,
                onExerciseClick = { selectedExercise = it }
            )
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
private fun DashboardLibrarySection(
    filterState: LibraryFilterState,
    filteredExercises: List<Exercise>,
    muscleGroups: List<String>,
    onSearchQueryChange: (String) -> Unit,
    onMuscleGroupChange: (String) -> Unit,
    onViewAll: () -> Unit,
    onExerciseClick: (Exercise) -> Unit
) {
    val exercisePages = remember(filteredExercises) { filteredExercises.chunked(5) }
    val pagerState = rememberPagerState(pageCount = { exercisePages.size.coerceAtLeast(1) })

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.dashboard_library_knowledge),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = stringResource(R.string.dashboard_library_title),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                }
                TextButton(onClick = onViewAll) {
                    Text(
                        text = stringResource(R.string.dashboard_library_view_all),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
            }

            OutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.dashboard_library_search_placeholder)) },
                shape = RoundedCornerShape(12.dp)
            )

            LibraryFilterRow(
                title = stringResource(R.string.dashboard_library_filter_muscle),
                options = muscleGroups,
                selected = filterState.muscleGroup,
                onSelect = onMuscleGroupChange
            )

//            Text(
//                text = stringResource(R.string.dashboard_library_results_count, filteredExercises.size),
//                fontSize = 11.sp,
//                fontWeight = FontWeight.Black,
//                color = MaterialTheme.colorScheme.secondary
//            )

            if (filteredExercises.isEmpty()) {
                Text(
                    text = stringResource(R.string.dashboard_library_empty_state),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    DashboardLibraryPage(
                        exercises = exercisePages[page],
                        onExerciseClick = onExerciseClick
                    )
                }

//                if (exercisePages.size > 1) {
//                    DashboardLibraryPagerIndicator(
//                        pageCount = exercisePages.size,
//                        currentPage = pagerState.currentPage
//                    )
//                }
            }
        }
    }
}

@Composable
private fun LibraryFilterRow(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelect(option) },
                    label = {
                        Text(
                            text = option,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardLibraryPage(
    exercises: List<Exercise>,
    onExerciseClick: (Exercise) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        exercises.forEach { exercise ->
            DashboardLibraryExerciseItem(
                exercise = exercise,
                onClick = { onExerciseClick(exercise) }
            )
        }
    }
}

@Composable
private fun DashboardLibraryExerciseItem(
    exercise: Exercise,
    onClick: () -> Unit
) {
    val badgeColor = when (exercise.difficulty) {
        "beginner" -> MaterialTheme.colorScheme.secondary
        "advanced" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    val muscles = remember(exercise.target_muscles) {
        exercise.target_muscles
            .filter { it != "Main" }
            .take(2)
            .joinToString(" · ")
    }

    val context = LocalContext.current
    val imageLoader = (context.applicationContext as FitFlowApplication).imageLoader

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(75.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
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
                Text(
                    text = exercise.exercise_type.take(3).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = exercise.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = muscles.ifBlank { exercise.exercise_type },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = exercise.difficulty.replaceFirstChar { it.uppercase() },
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = badgeColor,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(badgeColor.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DashboardLibraryPagerIndicator(
    pageCount: Int,
    currentPage: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { page ->
            val isSelected = page == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = if (isSelected) 18.dp else 8.dp, height = 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.18f)
                    )
            )
        }
    }
}

@Composable
fun TodayWeightSection(userProfile: UserProfile) {
    val weightLeft = userProfile.weight - userProfile.targetWeight
    val emoji = when {
        weightLeft > 5 -> "🔥"
        weightLeft > 0 -> "💪"
        else -> "🎉"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.dashboard_today_weight),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.dashboard_weight_kg_format, userProfile.weight),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        emoji,
                        fontSize = 24.sp
                    )
                }
                Text(
                    stringResource(R.string.dashboard_weight_to_goal_format, weightLeft),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    stringResource(R.string.dashboard_updated_today),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun HeaderSection(
    onOpenChatbot: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "FIT",
                color = Color(0xFFFF6B00),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic
            )
            Text(
                text = "FLOW",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic
            )
        }
        IconButton(
            onClick = onOpenChatbot,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    RoundedCornerShape(50)
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    RoundedCornerShape(50)
                )
        ) {
            Icon(
                Icons.Default.Chat,
                contentDescription = "AI Coach",
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun quoteForDay(dayIndex: Int): String = when (dayIndex) {
    0 -> stringResource(R.string.dashboard_quote_sunday)
    1 -> stringResource(R.string.dashboard_quote_monday)
    2 -> stringResource(R.string.dashboard_quote_tuesday)
    3 -> stringResource(R.string.dashboard_quote_wednesday)
    4 -> stringResource(R.string.dashboard_quote_thursday)
    5 -> stringResource(R.string.dashboard_quote_friday)
    6 -> stringResource(R.string.dashboard_quote_saturday)
    else -> stringResource(R.string.dashboard_quote_default)
}

@Composable
fun WeeklyGoalSection(
    weeklyGoal: Int = 3,
    globalWorkoutLogs: List<WorkoutLogEntry> = emptyList(),
    currentStreak: Int = 0,
    longestStreak: Int = 0,
    onViewHistory: () -> Unit = {},
    onEditGoal: () -> Unit = {},
    onToggleDay: (Int) -> Unit = {}
) {
    val today = LocalDate.now()
    val todayIndex = if (today.dayOfWeek.value == 7) 0 else today.dayOfWeek.value
    val startOfWeek = today.minusDays(todayIndex.toLong())
    val endOfWeek = startOfWeek.plusDays(6)

    val weekLogs = globalWorkoutLogs.filter {
        val d = LocalDate.ofEpochDay(it.dateEpochDay)
        !d.isBefore(startOfWeek) && !d.isAfter(endOfWeek)
    }
    val completedCount = weekLogs.distinctBy { it.dateEpochDay }.size
    val completedDateSetGlobal =
        globalWorkoutLogs.map { LocalDate.ofEpochDay(it.dateEpochDay) }.toSet()

    var todayOffsetX by remember { mutableStateOf(0f) }
    var rowWidth by remember { mutableStateOf(0f) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.dashboard_weekly_goal),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    IconButton(
                        onClick = onViewHistory,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Lucide.Eye,
                            contentDescription = "View History",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Days row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { cords ->
                        rowWidth = cords.size.width.toFloat()
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0..6) {
                    val date = startOfWeek.plusDays(i.toLong())
                    val isToday = i == todayIndex
                    val isPast = i < todayIndex

                    val isCompleted = date in completedDateSetGlobal

                    val bgColor = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.onBackground
                        else -> Color.Transparent
                    }
                    val textColor = when {
                        isCompleted || isToday -> MaterialTheme.colorScheme.background
                        isPast -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = if (isToday) {
                            Modifier.onGloballyPositioned { coords ->
                                // tâm của circle ngày hôm nay
                                todayOffsetX = coords.positionInParent().x + coords.size.width / 2f
                            }
                        } else Modifier
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(bgColor, CircleShape)
                                .clickable { onToggleDay(i) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${date.dayOfMonth}",
                                fontSize = 14.sp,
                                fontWeight = if (isToday || isCompleted) FontWeight.Bold else FontWeight.Normal,
                                color = textColor
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val arrowColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)
            if (rowWidth > 0f && todayOffsetX > 0f) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                ) {
                    val arrowW = 16.dp.toPx()
                    val arrowH = 8.dp.toPx()
                    val tipX = todayOffsetX.coerceIn(arrowW / 2f, size.width - arrowW / 2f)
                    val path = Path().apply {
                        moveTo(tipX, 0f)
                        lineTo(tipX + arrowW / 2f, arrowH)
                        lineTo(tipX - arrowW / 2f, arrowH)
                        close()
                    }
                    drawPath(path = path, color = arrowColor)
                }
            }

            // Streak info row
            if (currentStreak > 0 || longestStreak > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current streak
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "CURRENT STREAK",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🔥", fontSize = 16.sp)
                            Text(
                                text = "$currentStreak day${if (currentStreak != 1) "s" else ""}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF6B00)
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
                    )

                    // Longest streak
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "LONGEST STREAK",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$longestStreak day${if (longestStreak != 1) "s" else ""}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                            )
                            // NEW RECORD badge
                            if (currentStreak > 0 && currentStreak >= longestStreak) {
                                Text(
                                    text = "🏆",
                                    fontSize = 16.sp
                                )
                            }
                        }
                        if (currentStreak > 0 && currentStreak >= longestStreak) {
                            Text(
                                text = "NEW RECORD!",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color(0xFFFFAA00)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Coach quote dialog
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coach avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💪", fontSize = 22.sp)
                    }

                    // Quote
                    Text(
                        text = quoteForDay(todayIndex),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DailyChallengeSection(
    currentDay: Int,
    totalDays: Int,
    dayTitle: String,
    exercises: List<String>,
    onClick: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val completedDays = currentDay - 1
    val titleUppercase = remember(dayTitle) { dayTitle.uppercase() }
    val cardBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFF6D00),
                Color(0xFFFF3D00)
            )
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Daily Challenge",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filter",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = cardBrush)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 8.dp)
                    .size(width = 140.dp, height = 150.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.co_bung_2),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(
                                topStart = 32.dp,
                                bottomStart = 32.dp,
                                topEnd = 16.dp,
                                bottomEnd = 16.dp
                            )
                        )
                        .alpha(0.9f)
                )
            }

            // ── Foreground content ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Program name + difficulty
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        titleUppercase,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // Day number — big
                Text(
                    "Day $currentDay",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 48.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Progress counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "$completedDays",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "/$totalDays Days",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                // START button
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        "START",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutsSummarySection(
    completedCount: Int,
    totalWorkoutDays: Int,
    totalKcal: Int,
    onStartWorkout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.dashboard_workouts),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "$completedCount",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        stringResource(R.string.dashboard_days_suffix_format, totalWorkoutDays),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        stringResource(R.string.dashboard_completed),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "$totalKcal",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        stringResource(R.string.dashboard_kcal),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        stringResource(R.string.dashboard_burned),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onStartWorkout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        ) {
            Text(
                stringResource(R.string.dashboard_start_workout),
                color = MaterialTheme.colorScheme.background,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun HealthMetricsSection(
    metrics: DailyHealthMetrics,
    isActivityRecognitionGranted: Boolean,
    isStepSensorEnabled: Boolean,
    isStepTrackingActive: Boolean,
    onUnlockStepSensor: () -> Unit,
    onAddWater: (Int) -> Unit,
    onSetWaterGoal: (Int) -> Unit,
    onSetStepGoal: (Int) -> Unit = {}
) {
    // Dialog states
    var showWaterGoalDialog by remember { mutableStateOf(false) }
    var showStepGoalDialog by remember { mutableStateOf(false) }
    var showAddWaterDialog by remember { mutableStateOf(false) }
    var waterGoalInput by remember(metrics.waterGoalMl) { mutableStateOf(metrics.waterGoalMl.toString()) }
    var stepGoalInput by remember(metrics.stepGoal) { mutableStateOf(metrics.stepGoal.toString()) }
    var customWaterInput by remember { mutableStateOf("") }

    // Step progress
    val stepProgress = (metrics.steps.toFloat() / metrics.stepGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
    // Water progress
    val waterProgress = (metrics.waterIntakeMl.toFloat() / metrics.waterGoalMl.coerceAtLeast(1)).coerceIn(0f, 1f)

    Text(
        stringResource(R.string.dashboard_health_metrics),
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        fontWeight = FontWeight.Bold,
        letterSpacing = 3.sp
    )
    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Steps card ---
        MetricHorizontalCard(
            modifier = Modifier.weight(1f),
            icon = Lucide.Footprints,
            iconTint = MaterialTheme.colorScheme.primary,
            value = metrics.steps.toString(),
            unit = "/ ${metrics.stepGoal} STEPS",
            progress = stepProgress,
            progressColor = MaterialTheme.colorScheme.primary,
            buttonText = when {
                !isActivityRecognitionGranted -> stringResource(R.string.dashboard_unlock)
                !isStepSensorEnabled -> stringResource(R.string.dashboard_manual_mode)
                isStepTrackingActive -> stringResource(R.string.dashboard_live_sensor)
                else -> stringResource(R.string.dashboard_sensor_ready)
            },
            onClick = onUnlockStepSensor,
            secondaryButtonText = "SET GOAL",
            onSecondaryClick = { showStepGoalDialog = true },
            onLongClick = { showStepGoalDialog = true }
        )
        // --- Water card ---
        MetricHorizontalCard(
            modifier = Modifier.weight(1f),
            icon = Lucide.GlassWater,
            iconTint = MaterialTheme.colorScheme.secondary,
            value = metrics.waterIntakeMl.toString(),
            unit = "/ ${metrics.waterGoalMl} ML",
            progress = waterProgress,
            progressColor = MaterialTheme.colorScheme.secondary,
            buttonText = "+ ADD WATER",
            onClick = { showAddWaterDialog = true },
            onLongClick = { showWaterGoalDialog = true }
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Step tracking status hint
    Text(
        text = when {
            !isActivityRecognitionGranted -> stringResource(R.string.dashboard_steps_permission_off)
            !isStepSensorEnabled -> stringResource(R.string.dashboard_steps_unavailable)
            !isStepTrackingActive -> stringResource(R.string.dashboard_steps_tracking_paused)
            else -> stringResource(R.string.dashboard_steps_live_active)
        },
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
    )

    // ---- Dialogs ----

    // Add Water dialog
    if (showAddWaterDialog) {
        AlertDialog(
            onDismissRequest = { showAddWaterDialog = false; customWaterInput = "" },
            title = { Text("Add Water", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Quick presets
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(150, 250, 350, 500).forEach { amount ->
                            OutlinedButton(
                                onClick = {
                                    onAddWater(amount)
                                    showAddWaterDialog = false
                                    customWaterInput = ""
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text("${amount}ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    // Custom input
                    OutlinedTextField(
                        value = customWaterInput,
                        onValueChange = { customWaterInput = it.filter { c -> c.isDigit() } },
                        singleLine = true,
                        label = { Text("Custom amount (ml)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = customWaterInput.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            onAddWater(parsed)
                            showAddWaterDialog = false
                            customWaterInput = ""
                        }
                    },
                    enabled = customWaterInput.toIntOrNull()?.let { it > 0 } == true
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddWaterDialog = false; customWaterInput = "" }) {
                    Text(
                        stringResource(R.string.common_cancel)
                    )
                }
            }
        )
    }

    // Water Goal dialog
    if (showWaterGoalDialog) {
        AlertDialog(
            onDismissRequest = { showWaterGoalDialog = false },
            title = {
                Text(
                    stringResource(R.string.dashboard_water_goal_title),
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                OutlinedTextField(
                    value = waterGoalInput,
                    onValueChange = { waterGoalInput = it.filter { c -> c.isDigit() } },
                    singleLine = true,
                    label = { Text(stringResource(R.string.dashboard_goal_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = waterGoalInput.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            onSetWaterGoal(parsed)
                            showWaterGoalDialog = false
                        }
                    }
                ) { Text(stringResource(R.string.common_save)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showWaterGoalDialog = false
                }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    // Step Goal dialog
    if (showStepGoalDialog) {
        AlertDialog(
            onDismissRequest = { showStepGoalDialog = false },
            title = { Text("Set Step Goal", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Quick presets
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5000, 8000, 10000, 12000).forEach { goal ->
                            OutlinedButton(
                                onClick = {
                                    onSetStepGoal(goal)
                                    showStepGoalDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text("${goal/1000}K", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    // Custom input
                    OutlinedTextField(
                        value = stepGoalInput,
                        onValueChange = { stepGoalInput = it.filter { c -> c.isDigit() } },
                        singleLine = true,
                        label = { Text("Custom goal (steps)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = stepGoalInput.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            onSetStepGoal(parsed)
                            showStepGoalDialog = false
                        }
                    }
                ) { Text(stringResource(R.string.common_save)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showStepGoalDialog = false
                }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}

@Composable
fun MetricHorizontalCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    unit: String,
    progress: Float? = null,
    progressColor: Color = Color.Unspecified,
    buttonText: String,
    onClick: (() -> Unit)?,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top row: icon + optional goal pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Secondary button in top-right (e.g. SET GOAL)
                if (secondaryButtonText != null && onSecondaryClick != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f))
                            .clickable { onSecondaryClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            secondaryButtonText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
            // Value + unit
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    unit,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
            }
            // Progress bar
            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = progressColor,
                    trackColor = progressColor.copy(alpha = 0.12f)
                )
            }
            // Primary action button (full width)
            if (onClick != null) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        buttonText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        buttonText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    FitflowTheme {
        DashboardScreen()
    }
}

@Preview(showBackground = true, name = "Daily Challenge - Light Mode")
@Composable
fun DailyChallengeSectionPreview() {
    // Ép buộc preview sử dụng hệ màu nền sáng (Light Color Scheme) để đồng bộ với app mẫu
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = Color(0xFFF8F9FA), // Nền trắng xám nhạt cao cấp của app mẫu
            onBackground = Color(0xFF1A1A1A), // Chữ màu đen xám đậm dễ đọc
            primary = Color(0xFFFF5722), // Màu cam thương hiệu
            onPrimary = Color.White
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                DailyChallengeSection(
                    currentDay = 1,
                    totalDays = 30,
                    dayTitle = "Rock Hard Abs",
                    exercises = listOf("Crunch", "Plank", "Leg Raise"),
                    onClick = { /* Không xử lý hành động trong preview */ }
                )
            }
        }
    }
}