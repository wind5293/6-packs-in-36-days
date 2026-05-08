package com.example.fitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.Exercise
import kotlinx.coroutines.delay

@Composable
fun WorkoutDayDetailScreen(
    dayPlan: DayPlan,
    onBack: () -> Unit,
    onDayComplete: () -> Unit = {}
) {
    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var showRestTimer by remember { mutableStateOf(false) }
    // Trạng thái riêng cho "đã bấm Finish" để tránh gọi lại onDayComplete nhiều lần
    var dayFinished by remember { mutableStateOf(false) }

    val exercises = dayPlan.exercises
    val allDone = exercises.isNotEmpty() && currentExerciseIndex >= exercises.size

    if (showRestTimer) {
        RestTimerDialog(
            onFinish = { showRestTimer = false },
            onSkip   = { showRestTimer = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .padding(top = 16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "DAY ${dayPlan.dayNumber}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Text(
                    if (dayPlan.isRest) "REST & RECOVERY" else "WORKOUT SESSION",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic
                )
            }
        }

        when {
            dayPlan.isRest -> RestDayContent(onBack = onBack)
            allDone        -> DayCompleteContent(
                onFinish = {
                    if (!dayFinished) {
                        dayFinished = true
                        onDayComplete()
                    }
                    onBack()
                },
                onBack = onBack
            )
            else           -> WorkoutContent(
                exercises = exercises,
                currentIndex = currentExerciseIndex,
                onExerciseDone = {
                    currentExerciseIndex++
                    // Hiện rest timer nếu còn bài tiếp theo
                    if (currentExerciseIndex < exercises.size) {
                        showRestTimer = true
                    }
                    // Khi bài cuối xong → KHÔNG tự gọi onDayComplete,
                    // người dùng sẽ thấy DayCompleteContent và bấm FINISH
                }
            )
        }
    }
}

// ─── Rest Timer Dialog ────────────────────────────────────────────────────────

@Composable
private fun RestTimerDialog(onFinish: () -> Unit, onSkip: () -> Unit) {
    var secondsLeft by remember { mutableIntStateOf(60) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
        onFinish()
    }

    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    RoundedCornerShape(32.dp)
                )
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "REST",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "$secondsLeft",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic
            )
            Text(
                "seconds",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = { secondsLeft / 60f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSkip,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "SKIP REST",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ─── Workout Content ──────────────────────────────────────────────────────────

@Composable
private fun WorkoutContent(
    exercises: List<Exercise>,
    currentIndex: Int,
    onExerciseDone: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryChip("EXERCISES", "${exercises.size}")
        SummaryChip("TOTAL KCAL", "${exercises.sumOf { it.kcal }}")
        SummaryChip("DONE", "$currentIndex / ${exercises.size}")
    }

    Text(
        "EXERCISE LIST",
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(exercises) { index, exercise ->
            val state = when {
                index < currentIndex  -> ExerciseState.DONE
                index == currentIndex -> ExerciseState.ACTIVE
                else                  -> ExerciseState.LOCKED
            }
            ExerciseDetailCard(
                index = index + 1,
                exercise = exercise,
                state = state,
                onDone = onExerciseDone
            )
        }
    }
}

private enum class ExerciseState { DONE, ACTIVE, LOCKED }

@Composable
private fun ExerciseDetailCard(
    index: Int,
    exercise: Exercise,
    state: ExerciseState,
    onDone: () -> Unit
) {
    val cardAlpha  = if (state == ExerciseState.LOCKED) 0.35f else 1f
    val borderColor = when (state) {
        ExerciseState.DONE   -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
        ExerciseState.ACTIVE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        ExerciseState.LOCKED -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
    }
    val badgeColor = when (state) {
        ExerciseState.DONE   -> MaterialTheme.colorScheme.secondary
        ExerciseState.ACTIVE -> MaterialTheme.colorScheme.primary
        ExerciseState.LOCKED -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Index circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(badgeColor.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, badgeColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (state == ExerciseState.DONE) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text("$index", color = badgeColor, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        exercise.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${exercise.sets} sets  ×  ${exercise.reps} reps",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${exercise.kcal} kcal",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Nút DONE chỉ hiện ở bài tập đang active
            if (state == ExerciseState.ACTIVE) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(
                        "DONE",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

// ─── Supporting composables ───────────────────────────────────────────────────

@Composable
private fun SummaryChip(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.border(
            1.dp,
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
            RoundedCornerShape(16.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic
            )
            Text(
                label,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun RestDayContent(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💤", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "RECOVERY DAY",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Rest, hydrate, and let your muscles rebuild.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.7f).height(52.dp)
            ) {
                Text(
                    "BACK TO PLAN",
                    color = MaterialTheme.colorScheme.background,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
private fun DayCompleteContent(onFinish: () -> Unit, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text("🔥", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "DAY COMPLETE!",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Well done. Rest up for tomorrow.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            // Nút chính: FINISH WORKOUT — đánh dấu ngày hoàn thành + quay về Planner
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    "FINISH WORKOUT",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Nút phụ: chỉ quay về mà không đánh dấu hoàn thành
            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "BACK WITHOUT SAVING",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
