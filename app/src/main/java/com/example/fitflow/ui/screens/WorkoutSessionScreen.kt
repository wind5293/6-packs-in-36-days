package com.example.fitflow.ui.screens

import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fitflow.FitFlowApplication
import com.example.fitflow.data.model.WorkoutExercise
import com.example.fitflow.ui.theme.FitflowTheme
import com.example.fitflow.viewmodel.WorkoutSessionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

// ─── Thay đổi thời gian tại đây ───────────────────────────
private const val PREPARE_SECONDS = 5   // Đếm ngược trước mỗi bài
private const val REST_SECONDS = 25  // Nghỉ ngơi giữa các bài
// ──────────────────────────────────────────────────────────

enum class SessionPhase { PREPARING, EXERCISING, RESTING }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    exercises: List<WorkoutExercise> = sampleExercises(),
    onBack: () -> Unit = {},
    onFinish: () -> Unit = {},
    viewModel: WorkoutSessionViewModel = viewModel()
) {
  
    var showInstructionScreen by remember { mutableStateOf(false) }
    var index by remember { mutableStateOf(0) }
    var remaining by remember { mutableStateOf(exercises.getOrNull(0)?.durationSec ?: 0) }
    var isRunning by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf(SessionPhase.PREPARING) }
    var phaseTimer by remember { mutableStateOf(PREPARE_SECONDS) }

    val gifUrls by viewModel.gifUrls.collectAsState()
    Log.d("GIF_DEBUG", "gifUrls in UI: ${gifUrls.size}, keys: ${gifUrls.keys}")
    LaunchedEffect(Unit) {
        viewModel.loadGifs(exercises)
    }

    val current = exercises.getOrNull(index)
    val next = exercises.getOrNull(index + 1)

    if (showInstructionScreen && current != null) {
        ExerciseInstructionOverlayScreen(
            exercise = current,
            onClose = { showInstructionScreen = false }
        )
        return
    }

    val primaryColor  = MaterialTheme.colorScheme.primary
    val buttonBgColor = MaterialTheme.colorScheme.surfaceVariant
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = MaterialTheme.colorScheme.onBackground
    val bgColor = MaterialTheme.colorScheme.background

    // ── Hàm chuyển bài / kết thúc ──────────────────────────
    fun skipToNext() {
        if (index < exercises.lastIndex) {
            index++
            remaining = exercises[index].durationSec
            isRunning = false
            phase = SessionPhase.PREPARING
            phaseTimer = PREPARE_SECONDS
        } else {
            onFinish()
        }
    }

    fun goToPrev() {
        if (index > 0) {
            index--
            remaining = exercises[index].durationSec
            isRunning = false
            phase = SessionPhase.PREPARING
            phaseTimer = PREPARE_SECONDS
        }
    }

    // ── Timer chung xử lý cả 3 phase ───────────────────────
    LaunchedEffect(phase, index) {
        when (phase) {
            SessionPhase.PREPARING -> {
                phaseTimer = PREPARE_SECONDS
                while (isActive && phaseTimer > 0) {
                    delay(1000L); phaseTimer--
                }
                phase = SessionPhase.EXERCISING
                isRunning = current?.reps == 0
            }

            SessionPhase.EXERCISING -> { /* Timer xử lý riêng bên dưới */
            }

            SessionPhase.RESTING -> {
                phaseTimer = REST_SECONDS
                while (isActive && phaseTimer > 0) {
                    delay(1000L); phaseTimer--
                }
                skipToNext()
            }
        }
    }

    // Timer cho bài tập dựa-vào-thời-gian (time-based)
    LaunchedEffect(index, isRunning) {
        if (phase != SessionPhase.EXERCISING || !isRunning) return@LaunchedEffect
        while (isActive && remaining > 0) {
            delay(1000L)
            remaining = (remaining - 1).coerceAtLeast(0)
        }
        if (remaining == 0 && isRunning) {
            isRunning = false
            if (index < exercises.lastIndex) {
                phase = SessionPhase.RESTING
            } else {
                onFinish()
            }
        }
    }

    // ── Shared Progress Bar ─────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (phase == SessionPhase.RESTING) primaryColor else bgColor)
            .padding(top = 16.dp)
    ) {
        val totalSteps = (exercises.size * 2 - 1).coerceAtLeast(0)
        val currentStepIndex = if (phase == SessionPhase.RESTING) index * 2 + 1 else index * 2

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 0 until totalSteps) {
                Box(
                    modifier = Modifier
                        .weight(if (i % 2 == 0) 2f else 1f) // Bài tập dài hơn, nghỉ ngắn hơn
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when {
                                i < currentStepIndex -> primaryColor
                                i == currentStepIndex -> primaryColor.copy(alpha = 0.6f)
                                else -> buttonBgColor
                            }
                        )
                )
            }
        }

        when (phase) {
            // ─── Màn hình CHUẨN BỊ ─────────────────────────
            SessionPhase.PREPARING -> {
                ExerciseMediaArea(
                    current = current,
                    gifUrl = gifUrls[current?.name],
                    bgColor = bgColor,
                    buttonBgColor = buttonBgColor,
                    textColor = textColor,
                    iconColor = iconColor,
                    primaryColor = primaryColor,
                    prepareOverlay = true,
                    prepareCountdown = phaseTimer,
                    onBack = onBack,
                    modifier = Modifier.weight(1f)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GET READY",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = primaryColor,
                            fontSize = 32.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = current?.name ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showInstructionScreen = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.HelpOutline,
                                contentDescription = "Help",
                                modifier = Modifier.size(20.dp),
                                tint = iconColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            phase = SessionPhase.EXERCISING
                            isRunning = current?.reps == 0
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBgColor, contentColor = textColor
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Skip", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // ─── Màn hình TẬP LUYỆN ─────────────────────────
            SessionPhase.EXERCISING -> {
                ExerciseMediaArea(
                    current = current,
                    gifUrl = gifUrls[current?.name],
                    bgColor = bgColor,
                    buttonBgColor = buttonBgColor,
                    textColor = textColor,
                    iconColor = iconColor,
                    primaryColor = primaryColor,
                    prepareOverlay = false,
                    prepareCountdown = 0,
                    onBack = onBack,
                    modifier = Modifier.weight(1f)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val reps = current?.reps ?: 0
                        Text(
                            text = if (reps > 0) "× $reps" else "${remaining}s",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 56.sp, lineHeight = 56.sp
                            ),
                            color = textColor
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            IconButton(
                                onClick = {},
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(buttonBgColor)
                            ) {
                                Icon(
                                    Icons.Outlined.ThumbDown, contentDescription = "Too Hard",
                                    tint = textColor, modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = {},
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(buttonBgColor)
                            ) {
                                Icon(
                                    Icons.Outlined.ThumbUp, contentDescription = "Too Easy",
                                    tint = textColor, modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = current?.name ?: "Rest",
                            style = MaterialTheme.typography.headlineMedium,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showInstructionScreen = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.HelpOutline,
                                contentDescription = "Help",
                                modifier = Modifier.size(20.dp),
                                tint = iconColor
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedIconButton(
                        onClick = { goToPrev() },
                        modifier = Modifier.size(64.dp), shape = CircleShape
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = textColor
                        )
                    }

                    Button(
                        onClick = {
                            val reps = current?.reps ?: 0
                            if (reps > 0) {
                                // Xong bài rep → nghỉ (nếu còn bài tiếp)
                                if (index < exercises.lastIndex) {
                                    phase = SessionPhase.RESTING
                                } else onFinish()
                            } else {
                                if (remaining == 0) {
                                    if (index < exercises.lastIndex) {
                                        phase = SessionPhase.RESTING
                                    } else onFinish()
                                } else {
                                    isRunning = !isRunning
                                }
                            }
                        },
                        modifier = Modifier
                            .height(64.dp)
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        val reps = current?.reps ?: 0
                        val icon = if (reps > 0) Icons.Default.Check
                        else if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow
                        Icon(icon, contentDescription = "Action", modifier = Modifier.size(36.dp))
                    }

                    OutlinedIconButton(
                        onClick = {
                            if (index < exercises.lastIndex) {
                                phase = SessionPhase.RESTING
                            } else onFinish()
                        },
                        modifier = Modifier.size(64.dp), shape = CircleShape
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = textColor)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ─── Màn hình NGHỈ NGƠI ─────────────────────────
            SessionPhase.RESTING -> {
                RestScreen(
                    restSeconds = phaseTimer,
                    nextExercise = next,
                    nextGifUrl = gifUrls[next?.name],
                    nextIndex = index + 1,
                    totalExercises = exercises.size,
                    onAddTime = { phaseTimer += 20 },
                    onSkip = { skipToNext() },
                    onBack = onBack,
                    primaryColor = primaryColor
                )
            }
        }
    }
}

// ─── Vùng hiển thị GIF/Placeholder + Overlay ────────────────
@Composable
private fun ExerciseMediaArea(
    current: WorkoutExercise?,
    gifUrl: String?,
    bgColor: Color,
    buttonBgColor: Color,
    textColor: Color,
    iconColor: Color,
    primaryColor: Color,
    prepareOverlay: Boolean,
    prepareCountdown: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = (LocalContext.current.applicationContext as FitFlowApplication).imageLoader

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (gifUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(gifUrl)
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = "Exercise GIF",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        Icons.Default.Image, contentDescription = "Placeholder",
                        modifier = Modifier.size(64.dp), tint = iconColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "GIF OR IMG SPACE", color = iconColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            if (prepareOverlay) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColor.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$prepareCountdown",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 160.sp, lineHeight = 160.sp, color = primaryColor
                        )
                    )
                }
            }
        }

        // Top Action Buttons Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(buttonBgColor)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    Icons.Default.Settings to "Settings",
                    Icons.Default.Fullscreen to "Expand",
                    Icons.Default.MusicNote to "Music",
                    Icons.Default.Videocam to "Video"
                ).forEach { (icon, desc) ->
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(buttonBgColor)
                    ) {
                        Icon(icon, contentDescription = desc, tint = textColor)
                    }
                }
            }
        }
    }
}

// ─── Màn hình NGHỈ NGƠI ─────────────────────────────────────
@Composable
private fun RestScreen(
    restSeconds: Int,
    nextExercise: WorkoutExercise?,
    nextGifUrl: String?,
    nextIndex: Int,
    totalExercises: Int,
    onAddTime: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    primaryColor: Color
) {
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVar = MaterialTheme.colorScheme.surfaceVariant
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    val context = LocalContext.current

    val minutes = restSeconds / 60
    val secs = restSeconds % 60
    val timeStr = "%02d:%02d".format(minutes, secs)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(surfaceVar)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tiêu đề bài tiếp theo & Preview Card
        if (nextExercise != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "UP NEXT ${nextIndex}/${totalExercises}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = primaryColor,
                                letterSpacing = 0.08.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nextExercise.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic
                            ),
                        )
                    }
                    if (nextExercise.reps > 0) {
                        Text(
                            text = "x ${nextExercise.reps}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic
                            ),
                        )
                    } else if (nextExercise.durationSec > 0) {
                        Text(
                            text = "${nextExercise.durationSec}s",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // GIF preview bài tiếp theo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(surface),
                    contentAlignment = Alignment.Center
                ) {
                    if (nextGifUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(nextGifUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Next Exercise GIF",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.FitnessCenter, contentDescription = null,
                                modifier = Modifier.size(48.dp), tint = iconColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No image available",
                                color = iconColor,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            // Bài cuối cùng
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(surface),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.EmojiEvents, contentDescription = null,
                    modifier = Modifier.size(64.dp), tint = primaryColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Almost done!",
                    color = textColor,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // NGHỈ NGƠI title & Timer
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "REST",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = primaryColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = timeStr,
                style = MaterialTheme.typography.displayLarge.copy(
                    color = textColor,
                    fontSize = 80.sp,
                    lineHeight = 80.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Nút +20s và Bỏ qua
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onAddTime,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = surfaceVar,
                    contentColor = textColor
                ),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text("+20s", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Button(
                onClick = onSkip,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text("Skip", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

fun sampleExercises() = listOf(
    WorkoutExercise(
        category = "Strength", name = "Cable Seated Bicep Curl",
        sets = 3, reps = 12, kcal = 10, durationSec = 0,
        gifFileName = "cable_seated_bicep_curl_1.gif"
    ),
    WorkoutExercise(
        category = "Strength", name = "Cable Drag Curl",
        sets = 3, reps = 12, kcal = 10, durationSec = 0,
        gifFileName = "cable_drag_curl_1.gif"
    ),
    WorkoutExercise(
        category = "Strength", name = "Cable Bicep Curl (Close Grip)",
        sets = 3, reps = 12, kcal = 10, durationSec = 0,
        gifFileName = "cable_bicep_curl_(close_grip)_1.gif"
    ),
    WorkoutExercise(
        category = "Strength", name = "Dumbbell One-Arm Preacher Hammer Curl",
        sets = 3, reps = 12, kcal = 15, durationSec = 0,
        gifFileName = "dumbbell_one-arm_preacher_hammer_curl_1.gif"
    ),
    WorkoutExercise(
        category = "Strength", name = "Dumbbell Bicep Curl (Supine Wide Grip)",
        sets = 3, reps = 12, kcal = 15, durationSec = 0,
        gifFileName = "dumbbell_bicep_curl_(supine_wide_grip)_1.gif"
    ),
)

@Preview(showBackground = true)
@Composable
fun WorkoutSessionScreenPreview() {
    FitflowTheme { WorkoutSessionScreen() }
}
