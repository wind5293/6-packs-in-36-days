package com.example.fitflow

import android.app.Application
import com.example.fitflow.data.ExerciseRepository
import com.example.fitflow.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FitFlowApplication : Application() {

    lateinit var userPreferences: UserPreferences
        private set

    lateinit var exerciseRepository: ExerciseRepository
        private set

    override fun onCreate() {
        super.onCreate()
        userPreferences = UserPreferences(applicationContext)
        exerciseRepository = ExerciseRepository(applicationContext)

        // Chạy 1 lần duy nhất khi app cài lần đầu
        CoroutineScope(Dispatchers.IO).launch {
            exerciseRepository.prepopulateIfNeeded(applicationContext)
        }
    }
}