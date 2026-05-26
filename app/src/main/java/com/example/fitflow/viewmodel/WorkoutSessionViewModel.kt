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
}