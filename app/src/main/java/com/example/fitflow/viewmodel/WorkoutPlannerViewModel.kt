package com.example.fitflow.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fitflow.data.model.WorkoutExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel quản lý trạng thái chỉnh sửa kế hoạch ngày tập.
 * - Giữ bản copy `editablePlan` để không ảnh hưởng dữ liệu gốc.
 * - Hỗ trợ: di chuyển thứ tự, điều chỉnh reps/duration, thay thế bài tập.
 */
class WorkoutPlannerViewModel : ViewModel() {


    private val _editablePlan = MutableStateFlow<List<WorkoutExercise>>(emptyList())
    val editablePlan: StateFlow<List<WorkoutExercise>> = _editablePlan.asStateFlow()


    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()


    private val _savedExercises = MutableStateFlow<List<WorkoutExercise>?>(null)
    val savedExercises: StateFlow<List<WorkoutExercise>?> = _savedExercises.asStateFlow()


    fun enterEditMode(exercises: List<WorkoutExercise>) {
        _editablePlan.value = exercises.toMutableList()
        _isEditMode.value = true
        _savedExercises.value = null
    }


    fun exitEditMode() {
        _isEditMode.value = false
        _editablePlan.value = emptyList()
        _savedExercises.value = null
    }


    fun moveExercise(from: Int, to: Int) {
        val list = _editablePlan.value.toMutableList()
        if (from < 0 || to < 0 || from >= list.size || to >= list.size) return
        val item = list.removeAt(from)
        list.add(to, item)
        _editablePlan.value = list
    }
    fun adjustQuantity(index: Int, delta: Int) {
        _editablePlan.update { list ->
            list.mapIndexed { i, ex ->
                if (i != index) ex
                else if (ex.durationSec > 0) {
                    ex.copy(durationSec = (ex.durationSec + delta * 5).coerceAtLeast(5))
                } else {
                    ex.copy(reps = (ex.reps + delta).coerceAtLeast(1))
                }
            }
        }
    }


    fun replaceExercise(index: Int, newExercise: WorkoutExercise) {
        _editablePlan.update { list ->
            list.mapIndexed { i, ex -> if (i == index) newExercise else ex }
        }
    }


    fun saveChanges() {
        _savedExercises.value = _editablePlan.value.toList()
        _isEditMode.value = false
    }

    fun consumeSavedExercises() {
        _savedExercises.value = null
    }
}
