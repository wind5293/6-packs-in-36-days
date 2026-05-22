package com.example.fitflow

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
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

        // Setup Coil với GIF support
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .components {
                    add(GifDecoder.Factory())
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("gif_cache"))
                        .maxSizeBytes(300L * 1024 * 1024) // 300MB
                        .build()
                }
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.15)
                        .build()
                }
                .build()
        )

        CoroutineScope(Dispatchers.IO).launch {
            exerciseRepository.prepopulateIfNeeded(applicationContext)
        }
    }
}