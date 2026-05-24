package com.example.fitflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitflow.FitFlowApplication
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class Song(val name: String, val artist: String, val durationSec: Int)

class WorkoutSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = (application as FitFlowApplication).userPreferences

    // Available songs
    val songsList = listOf(
        Song("Dynamic Beats", "FitFlow DJ", 136),
        Song("Pump Up Rock", "Synthwave Club", 185),
        Song("Chill Lofi", "Acoustic Cafe", 160),
        Song("Hyper Speed", "Electronic Tribe", 144)
    )

    // Player State
    private val _currentSongIndex = MutableStateFlow(0)
    val currentSongIndex: StateFlow<Int> = _currentSongIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(46) // start at 46 as per screenshot
    val playbackProgress: StateFlow<Int> = _playbackProgress.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    // Persistent Settings
    private val _isBgMusicEnabled = MutableStateFlow(userPreferences.isBgMusicEnabled())
    val isBgMusicEnabled: StateFlow<Boolean> = _isBgMusicEnabled.asStateFlow()

    private val _bgMusicVolume = MutableStateFlow(userPreferences.getBgMusicVolume())
    val bgMusicVolume: StateFlow<Float> = _bgMusicVolume.asStateFlow()

    private val _isVoiceGuideEnabled = MutableStateFlow(userPreferences.isVoiceGuideEnabled())
    val isVoiceGuideEnabled: StateFlow<Boolean> = _isVoiceGuideEnabled.asStateFlow()

    private val _coachName = MutableStateFlow(userPreferences.getCoachName())
    val coachName: StateFlow<String> = _coachName.asStateFlow()

    private val _coachVolume = MutableStateFlow(userPreferences.getCoachVolume())
    val coachVolume: StateFlow<Float> = _coachVolume.asStateFlow()

    private val _isSoundEffectEnabled = MutableStateFlow(userPreferences.isSoundEffectEnabled())
    val isSoundEffectEnabled: StateFlow<Boolean> = _isSoundEffectEnabled.asStateFlow()

    private val _autoCounting = MutableStateFlow(userPreferences.getAutoCounting())
    val autoCounting: StateFlow<String> = _autoCounting.asStateFlow()

    private val _restTimer = MutableStateFlow(userPreferences.getRestTimer())
    val restTimer: StateFlow<String> = _restTimer.asStateFlow()

    private val _countdown = MutableStateFlow(userPreferences.getCountdown())
    val countdown: StateFlow<String> = _countdown.asStateFlow()

    private var playbackJob: Job? = null

    init {
        // Start or stop playback emulation coroutine based on isPlaying and isBgMusicEnabled
        viewModelScope.launch {
            combineStateFlows()
        }
    }

    private suspend fun combineStateFlows() {
        _isPlaying.collect { playing ->
            if (playing && _isBgMusicEnabled.value) {
                startPlaybackTimer()
            } else {
                stopPlaybackTimer()
            }
        }
    }

    private fun startPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentSong = songsList[_currentSongIndex.value]
                if (_playbackProgress.value < currentSong.durationSec) {
                    _playbackProgress.value += 1
                } else {
                    if (_isRepeat.value) {
                        _playbackProgress.value = 0
                    } else {
                        nextSong()
                    }
                }
            }
        }
    }

    private fun stopPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = null
    }

    // Controls
    fun togglePlayPause() {
        if (!_isBgMusicEnabled.value) return
        _isPlaying.update { !it }
    }

    fun nextSong() {
        if (songsList.isEmpty()) return
        _playbackProgress.value = 0
        if (_isShuffle.value) {
            _currentSongIndex.value = Random.nextInt(songsList.size)
        } else {
            _currentSongIndex.update { (it + 1) % songsList.size }
        }
        if (_isPlaying.value) {
            startPlaybackTimer()
        }
    }

    fun prevSong() {
        if (songsList.isEmpty()) return
        _playbackProgress.value = 0
        if (_isShuffle.value) {
            _currentSongIndex.value = Random.nextInt(songsList.size)
        } else {
            _currentSongIndex.update { (it - 1 + songsList.size) % songsList.size }
        }
        if (_isPlaying.value) {
            startPlaybackTimer()
        }
    }

    fun toggleShuffle() {
        _isShuffle.update { !it }
    }

    fun toggleRepeat() {
        _isRepeat.update { !it }
    }

    // Setters
    fun setBgMusicEnabled(enabled: Boolean) {
        userPreferences.setBgMusicEnabled(enabled)
        _isBgMusicEnabled.value = enabled
        if (!enabled) {
            _isPlaying.value = false
            stopPlaybackTimer()
        }
    }

    fun setBgMusicVolume(volume: Float) {
        userPreferences.setBgMusicVolume(volume)
        _bgMusicVolume.value = volume
    }

    fun setVoiceGuideEnabled(enabled: Boolean) {
        userPreferences.setVoiceGuideEnabled(enabled)
        _isVoiceGuideEnabled.value = enabled
    }

    fun setCoachName(name: String) {
        userPreferences.setCoachName(name)
        _coachName.value = name
    }

    fun setCoachVolume(volume: Float) {
        userPreferences.setCoachVolume(volume)
        _coachVolume.value = volume
    }

    fun setSoundEffectEnabled(enabled: Boolean) {
        userPreferences.setSoundEffectEnabled(enabled)
        _isSoundEffectEnabled.value = enabled
    }

    fun setAutoCounting(option: String) {
        userPreferences.setAutoCounting(option)
        _autoCounting.value = option
    }

    fun setRestTimer(option: String) {
        userPreferences.setRestTimer(option)
        _restTimer.value = option
    }

    fun setCountdown(option: String) {
        userPreferences.setCountdown(option)
        _countdown.value = option
    }
}
