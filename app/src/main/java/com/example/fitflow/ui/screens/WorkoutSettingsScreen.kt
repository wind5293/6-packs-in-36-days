package com.example.fitflow.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.composables.icons.lucide.*
import com.example.fitflow.ui.theme.OrangeGlow
import com.example.fitflow.ui.theme.OrangePrimary
import com.example.fitflow.ui.theme.CardBackground
import com.example.fitflow.ui.theme.CardNested
import com.example.fitflow.ui.theme.FitflowTheme
import com.example.fitflow.viewmodel.Song
import com.example.fitflow.viewmodel.WorkoutSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSettingsScreen(
    onBack: () -> Unit,
    viewModel: WorkoutSettingsViewModel,
    isInWorkoutSession: Boolean = false
) {
    // Collecting states from ViewModel
    val isBgMusicEnabled by viewModel.isBgMusicEnabled.collectAsState()
    val bgMusicVolume by viewModel.bgMusicVolume.collectAsState()
    val currentSongIndex by viewModel.currentSongIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()

    val isVoiceGuideEnabled by viewModel.isVoiceGuideEnabled.collectAsState()
    val coachVolume by viewModel.coachVolume.collectAsState()
    val isSoundEffectEnabled by viewModel.isSoundEffectEnabled.collectAsState()

    val restTimer by viewModel.restTimer.collectAsState()

    // Dialog trigger states
    var activeDialog by remember { mutableStateOf<String?>(null) } // "autoCounting", "restTimer", "countdown", "coach"

    val currentSong = viewModel.songsList.getOrNull(currentSongIndex) ?: Song("Unknown", "Unknown", 0)

    // Helper for formatting mm:ss
    val formatTime: (Int) -> String = { sec ->
        val m = sec / 60
        val s = sec % 60
        String.format("%02d:%02d", m, s)
    }

    LaunchedEffect(Unit) {
        viewModel.syncWithCurrentPlayback()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WORKOUT SETTINGS",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // PART 1: BACKGROUND MUSIC
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardBackground)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Lucide.Music,
                                    contentDescription = null,
                                    tint = if (isBgMusicEnabled) OrangePrimary else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                               )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Background Music",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Switch(
                                checked = isBgMusicEnabled,
                                onCheckedChange = { viewModel.setBgMusicEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = OrangePrimary,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.DarkGray
                                )
                            )
                        }

                        AnimatedVisibility(
                            visible = isBgMusicEnabled && isInWorkoutSession,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            // Compact player — chỉ hiện khi isInWorkoutSession=true (đảm bảo bởi visible condition)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardNested)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Album art + song info
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(OrangePrimary, Color(0xFFCC3300))
                                                )
                                            )
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Lucide.Music,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = currentSong.name.uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            fontStyle = FontStyle.Normal
                                        )
                                        Text(
                                            text = currentSong.artist,
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                // Progress bar
                                Slider(
                                    value = playbackProgress.toFloat(),
                                    onValueChange = {},
                                    valueRange = 0f..currentSong.durationSec.toFloat().coerceAtLeast(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.Transparent,
                                        activeTrackColor = OrangePrimary,
                                        inactiveTrackColor = Color.DarkGray
                                    ),
                                    modifier = Modifier.height(8.dp)
                                )
                                // Time labels
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = formatTime(playbackProgress), color = OrangePrimary, fontSize = 11.sp)
                                    Text(text = formatTime(currentSong.durationSec), color = Color.Gray, fontSize = 11.sp)
                                }
                                // Controls: Prev | Play/Pause | Next
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { viewModel.prevSong() }) {
                                        Icon(Lucide.SkipBack, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(OrangePrimary)
                                            .clickable { viewModel.togglePlayPause() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Lucide.Pause else Lucide.Play,
                                            contentDescription = "Play/Pause",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.nextSong() }) {
                                        Icon(Lucide.SkipForward, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                }
                                // Volume slider
                                VolumeSlider(
                                    value = bgMusicVolume,
                                    onValueChange = { viewModel.setBgMusicVolume(it) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Volume slider khi không trong buổi tập
                        AnimatedVisibility(
                            visible = isBgMusicEnabled && !isInWorkoutSession,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                VolumeSlider(
                                    value = bgMusicVolume,
                                    onValueChange = { viewModel.setBgMusicVolume(it) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // PART 2: VOICE & SOUND
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardBackground)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Voice Guide Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Voice Guide",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Switch(
                                checked = isVoiceGuideEnabled,
                                onCheckedChange = { viewModel.setVoiceGuideEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = OrangePrimary,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.DarkGray
                                )
                            )
                        }

                        // Coach option details
                        AnimatedVisibility(
                            visible = isVoiceGuideEnabled,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Coach Name row (Static to Sarah)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CardNested)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Coach voice",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Round Coach Avatar
                                        val avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=100&q=80"
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color.DarkGray)
                                        ) {
                                            AsyncImage(
                                                model = avatarUrl,
                                                contentDescription = "Sarah",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Text(
                                            text = "Sarah",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                // Coach volume Slider
                                VolumeSlider(
                                    value = coachVolume,
                                    onValueChange = { viewModel.setCoachVolume(it) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                        // Sound Effect Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sound Effect",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Switch(
                                checked = isSoundEffectEnabled,
                                onCheckedChange = { viewModel.setSoundEffectEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = OrangePrimary,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.DarkGray
                                )
                            )
                        }
                    }
                }

                // PART 3: GENERAL SETTINGS
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardBackground)
                            .padding(vertical = 8.dp)
                    ) {
                        // Rest Timer Item
                        GeneralSettingRow(
                            title = "Rest Timer",
                            subtitle = null,
                            valueText = restTimer,
                            onClick = { activeDialog = "restTimer" }
                        )
                    }
                }
            }
        }

        // Custom Dark Themed Selection Dialogs
        activeDialog?.let { dialogType ->
            var title = ""
            var options = listOf<String>()
            var selectedValue = ""
            var onSelect: (String) -> Unit = {}

            when (dialogType) {
                "restTimer" -> {
                    title = "REST TIMER"
                    options = listOf("Off", "10s", "20s", "30s", "40s", "50s", "60s")
                    selectedValue = restTimer
                    onSelect = { viewModel.setRestTimer(it) }
                }
            }

            AlertDialog(
                onDismissRequest = { activeDialog = null },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("CANCEL", color = OrangePrimary, fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Normal,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        options.forEach { option ->
                            val isSelected = option == selectedValue
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) OrangePrimary else CardNested)
                                    .clickable {
                                        onSelect(option)
                                        activeDialog = null
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    color = if (isSelected) Color.White else Color.LightGray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                if (isSelected) {
                                    Text(
                                        text = "✓",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                },
                containerColor = CardBackground,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun VolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Lucide.Volume1,
            contentDescription = "Volume Low",
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(28.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Track background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            )

            // Track filled
            Box(
                modifier = Modifier
                    .fillMaxWidth(value.coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(OrangePrimary)
            )

            // Thumb
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val thumbOffsetDp = maxWidth * value.coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .offset(x = thumbOffsetDp - 10.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, OrangePrimary.copy(alpha = 0.3f), CircleShape)
                )
            }

            // Invisible full-width slider để handle gesture
            Slider(
                value = value,
                onValueChange = onValueChange,
                colors = SliderDefaults.colors(
                    thumbColor = Color.Transparent,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            )
        }

        Icon(
            imageVector = Lucide.Volume2,
            contentDescription = "Volume High",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun GeneralSettingRow(
    title: String,
    subtitle: String?,
    valueText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = valueText,
                color = Color.Gray,
                fontSize = 14.sp
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
