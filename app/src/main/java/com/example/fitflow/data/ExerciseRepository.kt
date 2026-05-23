package com.example.fitflow.data

import android.content.Context
import com.example.fitflow.data.local.AppDatabase
import com.example.fitflow.data.local.ExerciseEntity
import com.example.fitflow.data.model.Exercise
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ExerciseRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).exerciseDao()

    suspend fun prepopulateIfNeeded(context: Context) {
        if (dao.count() > 0) return  // Đã có data, bỏ qua

        withContext(Dispatchers.IO) {
            val json = context.assets.open("exercises.json")
                .bufferedReader().use { it.readText() }

            val entities = Gson()
                .fromJson(json, Array<Exercise>::class.java)
                .map { ExerciseEntity.fromExercise(it) }

            val exercises = Gson().fromJson(json, Array<Exercise>::class.java)

            // Log tạm để kiểm tra
            val noGif = exercises.filter { it.local_gifs.isEmpty() }
            android.util.Log.d("DB_CHECK", "Total: ${exercises.size}")
            android.util.Log.d("DB_CHECK", "No GIF: ${noGif.size}")
            noGif.forEach { android.util.Log.d("DB_CHECK", "Missing GIF: ${it.name}") }

            dao.insertAll(entities)
        }
    }

    fun getAll(): Flow<List<Exercise>> =
        dao.getAll().map { list -> list.map { it.toExercise() } }

    fun search(query: String): Flow<List<Exercise>> =
        dao.search(query).map { list -> list.map { it.toExercise() } }

    fun getByDifficulty(difficulty: String): Flow<List<Exercise>> =
        dao.getByDifficulty(difficulty).map { list -> list.map { it.toExercise() } }

    fun getByType(type: String): Flow<List<Exercise>> =
        dao.getByType(type).map { list -> list.map { it.toExercise() } }

    suspend fun getByName(name: String): Exercise? =
        dao.getByName(name)?.toExercise()

    suspend fun getGifFileName(name: String): String? =
        dao.getByName(name)?.local_gifs?.firstOrNull()
}