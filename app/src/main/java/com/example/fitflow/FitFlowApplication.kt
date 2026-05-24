package com.example.fitflow

import android.app.Application
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
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

    lateinit var imageLoader: ImageLoader  // ✅ thêm dòng này
        private set

    override fun onCreate() {
        super.onCreate()

        userPreferences = UserPreferences(applicationContext)
        exerciseRepository = ExerciseRepository(applicationContext)

        imageLoader = ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                else add(GifDecoder.Factory())
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("gif_cache"))
                    .maxSizeBytes(300L * 1024 * 1024)
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15)
                    .build()
            }
            .build()

        Coil.setImageLoader(imageLoader)

        CoroutineScope(Dispatchers.IO).launch {
            exerciseRepository.prepopulateIfNeeded(applicationContext)
        }
    }
}