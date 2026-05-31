package com.example.fitflow.viewmodel

import android.app.Application
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
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
        Song("It's Her", "Hallmore", 152),
        Song("Restless Clocks", "Killrude", 172),
        Song("Rest My Case", "Killrude", 234),
    )

    private val assetPaths = listOf(
        "music/track_01.mp3",
        "music/track_02.mp3",
        "music/track_03.mp3"
    )

    // Player State
    private val _currentSongIndex = MutableStateFlow(0)
    val currentSongIndex: StateFlow<Int> = _currentSongIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(40)
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

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    fun startMusicIfEnabled() {
        android.util.Log.d("MUSIC_DEBUG", "startMusicIfEnabled: isBgEnabled=${_isBgMusicEnabled.value}, mediaPlayer=$mediaPlayer")
        if (_isBgMusicEnabled.value && mediaPlayer == null) {
            loadAndPlay(_currentSongIndex.value)
        }
    }

    fun stopMusic() {
        progressJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        _playbackProgress.value = 0
    }

    private fun loadAndPlay(index: Int) {
        progressJob?.cancel()
        mediaPlayer?.release()

        try {
            val afd: AssetFileDescriptor =
                getApplication<Application>().assets.openFd(assetPaths[index])
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                val v = _bgMusicVolume.value
                setVolume(v, v)
                isLooping = _isRepeat.value
                setOnCompletionListener {
                    if (!_isRepeat.value) nextSong()
                }
                prepare()
                start()
            }
            afd.close()
            _currentSongIndex.value = index
            _isPlaying.value = true
            _playbackProgress.value = 0
            startProgressTracking()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                delay(500)
                _playbackProgress.value = (mediaPlayer?.currentPosition ?: 0) / 1000
            }
        }
    }

    fun syncWithCurrentPlayback() {
        val pos = mediaPlayer?.currentPosition ?: -1
        val playing = mediaPlayer?.isPlaying ?: false
        android.util.Log.d("MUSIC_DEBUG", "syncWithCurrentPlayback: mediaPlayer=$mediaPlayer, pos=$pos, isPlaying=$playing, _isPlaying=${_isPlaying.value}")
        if (mediaPlayer != null && _isPlaying.value) {
            startProgressTracking()
        }
        _playbackProgress.value = (mediaPlayer?.currentPosition ?: 0) / 1000
    }

    // ── Controls ─────────────────────────────────────────────
    fun togglePlayPause() {
        if (!_isBgMusicEnabled.value) return
        val mp = mediaPlayer ?: run { loadAndPlay(_currentSongIndex.value); return }
        if (mp.isPlaying) {
            mp.pause()
            _isPlaying.value = false
            progressJob?.cancel()
        } else {
            mp.start()
            _isPlaying.value = true
            startProgressTracking()
        }
    }

    fun pauseMusic() {
        mediaPlayer?.pause()
        _isPlaying.value = false
        progressJob?.cancel()
    }

    fun resumeMusic() {
        if (_isBgMusicEnabled.value && mediaPlayer != null) {
            mediaPlayer?.start()
            _isPlaying.value = true
            startProgressTracking()
        }
    }

    fun nextSong() {
        val next = if (_isShuffle.value)
            (0 until songsList.size).filter { it != _currentSongIndex.value }
                .randomOrNull() ?: 0
        else (_currentSongIndex.value + 1) % songsList.size
        loadAndPlay(next)
    }

    fun prevSong() {
        // Nếu đã phát > 3 giây thì rewind, không thì về bài trước
        val currentSec = mediaPlayer?.currentPosition?.div(1000) ?: 0
        if (currentSec > 3) {
            mediaPlayer?.seekTo(0)
            _playbackProgress.value = 0
        } else {
            val prev = (_currentSongIndex.value - 1 + songsList.size) % songsList.size
            loadAndPlay(prev)
        }
    }

    fun toggleShuffle() { _isShuffle.update { !it } }

    fun toggleRepeat() {
        _isRepeat.update { !it }
        mediaPlayer?.isLooping = _isRepeat.value
    }

    // ── Setters ───────────────────────────────────────────────
    fun setBgMusicEnabled(enabled: Boolean) {
        userPreferences.setBgMusicEnabled(enabled)
        _isBgMusicEnabled.value = enabled
        if (enabled) {
            loadAndPlay(_currentSongIndex.value)
        } else {
            mediaPlayer?.pause()
            _isPlaying.value = false
            progressJob?.cancel()
        }
    }

    fun setBgMusicVolume(volume: Float) {
        userPreferences.setBgMusicVolume(volume)
        _bgMusicVolume.value = volume
        mediaPlayer?.setVolume(volume, volume)  // ← apply ngay lập tức
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

    override fun onCleared() {
        super.onCleared()
        stopMusic()
    }
}
