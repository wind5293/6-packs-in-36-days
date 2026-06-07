package com.example.fitflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitflow.FitFlowApplication
import com.example.fitflow.data.model.Exercise
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class LibraryFilterState(
    val searchQuery: String = "",
    val category: String = "ALL",
    val difficulty: String = "ALL",
    val muscleGroup: String = "ALL",
)

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as FitFlowApplication).exerciseRepository

    // Filter state
    private val _filterState = MutableStateFlow(LibraryFilterState())
    val filterState: StateFlow<LibraryFilterState> = _filterState.asStateFlow()

    // Toàn bộ danh sách — dùng để lấy categories, difficulties, muscleGroups
    val allExercises: StateFlow<List<Exercise>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Danh sách sau khi filter — tự động cập nhật khi filterState thay đổi
    val filteredExercises: StateFlow<List<Exercise>> = _filterState.flatMapLatest { filter ->
            repository.getAll().map { list ->
                list.filter { exercise ->
                    val categoryMatch =
                        filter.category == "ALL" || exercise.exercise_type == filter.category
                    val difficultyMatch =
                        filter.difficulty == "ALL" || exercise.difficulty == filter.difficulty
                    val muscleMatch =
                        filter.muscleGroup == "ALL" || exercise.target_muscles.contains(filter.muscleGroup)
                    val searchMatch = filter.searchQuery.isEmpty() || exercise.name.contains(
                        filter.searchQuery, ignoreCase = true
                    )
                    categoryMatch && difficultyMatch && muscleMatch && searchMatch
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter options — derive từ allExercises
    val categories: StateFlow<List<String>> = allExercises.map { list ->
            listOf("ALL") + list.map { it.exercise_type }.distinct().sorted()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("ALL"))

    val difficulties: StateFlow<List<String>> = allExercises.map { list ->
            val order = listOf("beginner", "intermediate", "advanced")
            listOf("ALL") + list.map { it.difficulty }.distinct().sortedBy { order.indexOf(it) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("ALL"))

    val muscleGroups: StateFlow<List<String>> = allExercises.map { list ->
            listOf("ALL") + list.flatMap { it.target_muscles }.distinct().sorted()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("ALL"))

    // Update functions
    fun setSearchQuery(query: String) = _filterState.update { it.copy(searchQuery = query) }
    fun setCategory(category: String) = _filterState.update { it.copy(category = category) }
    fun setDifficulty(difficulty: String) = _filterState.update { it.copy(difficulty = difficulty) }
    fun setMuscleGroup(group: String) = _filterState.update { it.copy(muscleGroup = group) }
}
