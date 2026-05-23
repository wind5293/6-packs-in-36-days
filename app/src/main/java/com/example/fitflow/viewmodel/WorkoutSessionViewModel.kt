package com.example.fitflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitflow.FitFlowApplication
import com.example.fitflow.data.model.WorkoutExercise
import com.example.fitflow.utils.GifUrlHelper
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
     * ViewModel sẽ lookup Room lấy tên GIF, rồi build URL qua GifUrlHelper.
     */
    fun loadGifs(exercises: List<WorkoutExercise>) {
        viewModelScope.launch {
            val urls = mutableMapOf<String, String>()
            exercises.forEach { exercise ->
                val fileName = repository.getGifFileName(exercise.name)
                if (!fileName.isNullOrEmpty()) {
                    urls[exercise.name] = GifUrlHelper.getUrl(fileName)
                }
            }
            _gifUrls.value = urls
        }
    }
}