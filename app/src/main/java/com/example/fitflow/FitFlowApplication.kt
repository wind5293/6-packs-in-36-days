package com.example.fitflow

import android.app.Application
import com.example.fitflow.data.UserPreferences

class FitFlowApplication : Application() {
    lateinit var userPreferences: UserPreferences
        private set
    override fun onCreate() {
        super.onCreate()
        userPreferences = UserPreferences(applicationContext)
    }
}