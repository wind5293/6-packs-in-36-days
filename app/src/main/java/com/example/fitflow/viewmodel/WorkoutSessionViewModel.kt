package com.example.fitflow.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitflow.FitFlowApplication
import com.example.fitflow.data.model.WorkoutExercise
import com.example.fitflow.utils.GifSourceResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as FitFlowApplication).exerciseRepository

    // Map: tên bài tập → GIF URL
    private val _gifUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val gifUrls: StateFlow<Map<String, String>> = _gifUrls

    /**
     * Gọi 1 lần khi vào màn hình, truyền vào danh sách bài tập của buổi tập.
     * ViewModel resolve local file trước, fallback sang URL remote khi file chưa có.
     */
    fun loadGifs(exercises: List<WorkoutExercise>) {
        viewModelScope.launch {
            Log.d("GIF_DEBUG", "=== loadGifs START, exercises: ${exercises.size}")
            val urls = mutableMapOf<String, String>()
            exercises.forEach { exercise ->
                val trimmed = exercise.gifFileName.trim()
                val source = GifSourceResolver.resolve(trimmed, getApplication())
                if (source != null) {
                    Log.d("GIF_DEBUG", "Adding: ${exercise.name} → $source")
                    urls[exercise.name] = source
                }
            }
            Log.d("GIF_DEBUG", "=== Before assign, map size: ${urls.size}")
            _gifUrls.value = urls
            Log.d("GIF_DEBUG", "=== After assign, gifUrls size: ${_gifUrls.value.size}")
        }
    }

    private val userPreferences = (application as FitFlowApplication).userPreferences

    private val restDuration: Int = run {
        val raw = userPreferences.getRestTimer()
        when {
            raw.equals("Off", ignoreCase = true) -> 0
            raw.endsWith("s") -> raw.dropLast(1).toIntOrNull() ?: 30
            else -> 30
        }
    }

    private val _restRemaining = MutableStateFlow(0)
    val restRemaining: StateFlow<Int> = _restRemaining.asStateFlow()

    private val _isResting = MutableStateFlow(false)
    val isResting: StateFlow<Boolean> = _isResting.asStateFlow()

    private var restJob: Job? = null

    fun startRest() {
        if (restDuration == 0) { _isResting.value = false; return }
        _restRemaining.value = restDuration
        _isResting.value = true
        restJob?.cancel()
        restJob = viewModelScope.launch {
            while (_restRemaining.value > 0) {
                delay(1000)
                _restRemaining.value -= 1
            }
            _isResting.value = false
        }
    }

    fun addRestTime(seconds: Int = 20) {
        _restRemaining.value += seconds
        if (restJob?.isActive != true && _restRemaining.value > 0) {
            _isResting.value = true
            restJob = viewModelScope.launch {
                while (_restRemaining.value > 0) {
                    delay(1000)
                    _restRemaining.value -= 1
                }
                _isResting.value = false
            }
        }
    }

    fun skipRest() {
        restJob?.cancel()
        _restRemaining.value = 0
        _isResting.value = false
    }

    override fun onCleared() {
        restJob?.cancel()
        super.onCleared()
    }

    private val countdownDuration: Int = run {
        val raw = userPreferences.getCountdown()
        when {
            raw.equals("Off", ignoreCase = true) -> 0
            raw.endsWith("s") -> raw.dropLast(1).toIntOrNull() ?: 5
            else -> 5
        }
    }

    private val _countdownRemaining = MutableStateFlow(0)
    val countdownRemaining: StateFlow<Int> = _countdownRemaining.asStateFlow()

    private val _isCountingDown = MutableStateFlow(true)
    val isCountingDown: StateFlow<Boolean> = _isCountingDown.asStateFlow()

    private var countdownJob: Job? = null

    fun startCountdown() {
        if (countdownDuration == 0) {
            _isCountingDown.value = false;
            _countdownRemaining.value = 0
            return
        }

        countdownJob?.cancel()
        _countdownRemaining.value = countdownDuration
        _isCountingDown.value = true

        countdownJob = viewModelScope.launch {
            while (_countdownRemaining.value > 0) {
                delay(1000)
                _countdownRemaining.value -= 1
            }
            _isCountingDown.value = false
        }
    }

    fun skipCountdown() {
        countdownJob?.cancel()
        _countdownRemaining.value = 0
        _isCountingDown.value = false
    }
}