package com.example.fitflow.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.composables.icons.lucide.*
import com.example.fitflow.ui.theme.OrangeGlow
import com.example.fitflow.ui.theme.OrangePrimary
import com.example.fitflow.ui.theme.CardBackground
import com.example.fitflow.ui.theme.CardNested
import com.example.fitflow.viewmodel.Song
import com.example.fitflow.viewmodel.WorkoutSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSettingsScreen(
    onBack: () -> Unit,
    viewModel: WorkoutSettingsViewModel = viewModel()
) {
    // Collecting states from ViewModel
    val isBgMusicEnabled by viewModel.isBgMusicEnabled.collectAsState()
    val bgMusicVolume by viewModel.bgMusicVolume.collectAsState()
    val currentSongIndex by viewModel.currentSongIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val isRepeat by viewModel.isRepeat.collectAsState()

    val isVoiceGuideEnabled by viewModel.isVoiceGuideEnabled.collectAsState()
    val coachName by viewModel.coachName.collectAsState()
    val coachVolume by viewModel.coachVolume.collectAsState()
    val isSoundEffectEnabled by viewModel.isSoundEffectEnabled.collectAsState()

    val autoCounting by viewModel.autoCounting.collectAsState()
    val restTimer by viewModel.restTimer.collectAsState()
    val countdown by viewModel.countdown.collectAsState()

    // Dialog trigger states
    var activeDialog by remember { mutableStateOf<String?>(null) } // "autoCounting", "restTimer", "countdown", "coach"

    val currentSong = viewModel.songsList.getOrNull(currentSongIndex) ?: Song("Unknown", "Unknown", 0)

    // Helper for formatting mm:ss
    val formatTime: (Int) -> String = { sec ->
        val m = sec / 60
        val s = sec % 60
        String.format("%02d:%02d", m, s)
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
                        imageVector = Icons.Default.ArrowBack,
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
                            visible = isBgMusicEnabled,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            ) {
                                // Music Player Container
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CardNested)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Album art Box with pulsing orange glow
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
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
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentSong.name.uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            fontStyle = FontStyle.Italic
                                        )
                                        Text(
                                            text = currentSong.artist,
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // Simulated playing audio wave indicator
                                            if (isPlaying) {
                                                Text(
                                                    text = "❙❙❙",
                                                    color = OrangePrimary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "${formatTime(playbackProgress)} / ${formatTime(currentSong.durationSec)}",
                                                color = OrangePrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Progress Slider (un-thumbed or mini-slider for aesthetic progress representation)
                                Slider(
                                    value = playbackProgress.toFloat(),
                                    onValueChange = {},
                                    valueRange = 0f..currentSong.durationSec.toFloat().coerceAtLeast(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.Transparent, // hidden thumb for look and feel
                                        activeTrackColor = OrangePrimary,
                                        inactiveTrackColor = Color.DarkGray
                                    ),
                                    modifier = Modifier.height(8.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Control Pad
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                                        Icon(
                                            imageVector = Lucide.Shuffle,
                                            contentDescription = "Shuffle",
                                            tint = if (isShuffle) OrangePrimary else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.prevSong() }) {
                                        Icon(
                                            imageVector = Lucide.SkipBack,
                                            contentDescription = "Prev",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    // Play Pause Glowing Circle
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(OrangePrimary)
                                            .clickable { viewModel.togglePlayPause() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Lucide.Pause else Lucide.Play,
                                            contentDescription = "Play/Pause",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.nextSong() }) {
                                        Icon(
                                            imageVector = Lucide.SkipForward,
                                            contentDescription = "Next",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.toggleRepeat() }) {
                                        Icon(
                                            imageVector = Lucide.Repeat,
                                            contentDescription = "Repeat",
                                            tint = if (isRepeat) OrangePrimary else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Volume Slider
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Lucide.Volume2,
                                        contentDescription = "Volume Low",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Slider(
                                        value = bgMusicVolume,
                                        onValueChange = { viewModel.setBgMusicVolume(it) },
                                        colors = SliderDefaults.colors(
                                            thumbColor = OrangePrimary,
                                            activeTrackColor = OrangePrimary,
                                            inactiveTrackColor = Color.DarkGray
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp)
                                    )
                                    Icon(
                                        imageVector = Lucide.Volume2,
                                        contentDescription = "Volume High",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
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
                                // Coach Name selector row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CardNested)
                                        .clickable { activeDialog = "coach" }
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
                                        val avatarUrl = when (coachName) {
                                            "James" -> "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&q=80"
                                            "Sarah" -> "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=100&q=80"
                                            else -> "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=100&q=80"
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color.DarkGray)
                                        ) {
                                            AsyncImage(
                                                model = avatarUrl,
                                                contentDescription = coachName,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Text(
                                            text = coachName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
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

                                // Coach volume Slider
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Lucide.Volume2,
                                        contentDescription = "Volume Low",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Slider(
                                        value = coachVolume,
                                        onValueChange = { viewModel.setCoachVolume(it) },
                                        colors = SliderDefaults.colors(
                                            thumbColor = OrangePrimary,
                                            activeTrackColor = OrangePrimary,
                                            inactiveTrackColor = Color.DarkGray
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp)
                                    )
                                    Icon(
                                        imageVector = Lucide.Volume2,
                                        contentDescription = "Volume High",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.05f))

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
                        // Auto Counting Item
                        GeneralSettingRow(
                            title = "Auto Counting",
                            subtitle = null,
                            valueText = autoCounting,
                            onClick = { activeDialog = "autoCounting" }
                        )

                        Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))

                        // Rest Timer Item
                        GeneralSettingRow(
                            title = "Rest Timer",
                            subtitle = null,
                            valueText = restTimer,
                            onClick = { activeDialog = "restTimer" }
                        )

                        Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))

                        // Countdown before exercise Item
                        GeneralSettingRow(
                            title = "Countdown",
                            subtitle = "Before exercise",
                            valueText = countdown,
                            onClick = { activeDialog = "countdown" }
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
                "coach" -> {
                    title = "SELECT COACH"
                    options = listOf("James", "Sarah", "Emily")
                    selectedValue = coachName
                    onSelect = { viewModel.setCoachName(it) }
                }
                "autoCounting" -> {
                    title = "AUTO COUNTING"
                    options = listOf("Off", "On")
                    selectedValue = autoCounting
                    onSelect = { viewModel.setAutoCounting(it) }
                }
                "restTimer" -> {
                    title = "REST TIMER"
                    options = listOf("Off", "10s", "20s", "30s", "40s", "50s", "60s")
                    selectedValue = restTimer
                    onSelect = { viewModel.setRestTimer(it) }
                }
                "countdown" -> {
                    title = "COUNTDOWN BEFORE EXERCISE"
                    options = listOf("Off", "3s", "5s", "10s", "15s")
                    selectedValue = countdown
                    onSelect = { viewModel.setCountdown(it) }
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
                        fontStyle = FontStyle.Italic,
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
