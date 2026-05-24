package com.example.fitflow.data

import android.content.Context
import com.example.fitflow.data.local.AppDatabase
import com.example.fitflow.data.local.ExerciseEntity
import com.example.fitflow.data.model.Exercise
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ExerciseRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).exerciseDao()
    private var cachedExercisesWithGifs: List<Exercise>? = null

    // Deterministic aliases for plan naming that differs from exercise DB labels.
    private val directAliasToExerciseName: Map<String, String> = mapOf(
        "treadmilljog" to "Treadmill Running",
        "treadmillsprint" to "Treadmill Running",
        "dumbbellflyes" to "Dumbbell Fly",
        "plankhold" to "Plank",
        "lungejump" to "Bodyweight Lunge",
        "skaterjump" to "Side Hop-Sprint",
        "planktodownwarddog" to "Downward Facing Dog",
        "battleropewave" to "Side Hop-Sprint",
        "lateralshuffle" to "Side Hop-Sprint",
        "hollowbodyhold" to "Plank"
    )

    private val aliasMap: Map<String, List<String>> = mapOf(
        "jumpingjack" to listOf("jumping jack", "jumping jacks"),
        "pushup" to listOf("push-up", "push up", "pushup"),
        "triceppushdown" to listOf("tricep pushdown", "triceps pushdown", "cable tricep pushdown"),
        "overheadextension" to listOf("overhead tricep extension", "tricep overhead extension", "overhead extension"),
        "skullcrusher" to listOf("skull crusher", "lying tricep extension", "tricep extension"),
        "barbellbentoverrow" to listOf("bent-over row", "barbell row"),
        "fullbodycircuit" to listOf("full body", "circuit")
    )

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
            cachedExercisesWithGifs = null
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

    suspend fun getGifFileName(name: String): String? {
        dao.getByName(name)?.local_gifs?.firstOrNull()?.let { return it }
        dao.getByNameIgnoreCase(name)?.local_gifs?.firstOrNull()?.let { return it }

        val candidates = getExercisesWithGifs()
        if (candidates.isEmpty()) return null

        val normalizedTarget = normalizeName(name)
        candidates.firstOrNull { normalizeName(it.name) == normalizedTarget }
            ?.local_gifs
            ?.firstOrNull()
            ?.let { return it }

        directAliasToExerciseName[normalizedTarget]
            ?.let { aliasName ->
                dao.getByNameIgnoreCase(aliasName)
                    ?.local_gifs
                    ?.firstOrNull()
                    ?.let { return it }
            }

        val aliasHints = aliasMap[normalizedTarget].orEmpty()
        if (aliasHints.isNotEmpty()) {
            val aliasMatch = candidates.firstOrNull { candidate ->
                val candidateName = candidate.name.lowercase()
                aliasHints.any { hint -> candidateName.contains(hint) }
            }
            aliasMatch?.local_gifs?.firstOrNull()?.let { return it }
        }

        val fuzzy = findBestByTokenScore(name, candidates)
        return fuzzy?.local_gifs?.firstOrNull()
    }
        
    suspend fun findBestMatchByName(name: String): Exercise? {
        dao.getByName(name)?.let { return it.toExercise() }
        dao.getByNameIgnoreCase(name)?.let { return it.toExercise() }

        val candidates = getAll().first()
        val normalizedTarget = normalizeName(name)
        candidates.firstOrNull { exercise ->
            normalizeName(exercise.name) == normalizedTarget
        }?.let { return it }

        return findBestByTokenScore(name, candidates)
    }

    private suspend fun getExercisesWithGifs(): List<Exercise> {
        cachedExercisesWithGifs?.let { return it }
        val loaded = dao.getAll().first()
            .map { it.toExercise() }
            .filter { it.local_gifs.isNotEmpty() }
        cachedExercisesWithGifs = loaded
        return loaded
    }

    private fun findBestByTokenScore(name: String, candidates: List<Exercise>): Exercise? {
        val queryTokens = tokenize(name)
        if (queryTokens.isEmpty()) return null

        val scored = candidates.mapNotNull { candidate ->
            val candidateTokens = tokenize(candidate.name)
            if (candidateTokens.isEmpty()) return@mapNotNull null

            val overlap = queryTokens.intersect(candidateTokens.toSet()).size
            if (overlap == 0) return@mapNotNull null

            val qSize = queryTokens.size
            val cSize = candidateTokens.size
            val dice = (2f * overlap.toFloat()) / (qSize + cSize).toFloat()

            val queryNorm = normalizeName(name)
            val candidateNorm = normalizeName(candidate.name)
            val containsBonus = if (
                candidateNorm.contains(queryNorm) || queryNorm.contains(candidateNorm)
            ) 0.2f else 0f

            val score = dice + containsBonus
            Triple(candidate, overlap, score)
        }

        val best = scored.maxByOrNull { it.third } ?: return null
        val minimumOverlap = if (queryTokens.size >= 2) 2 else 1
        val minimumScore = 0.38f

        return if (best.second >= minimumOverlap && best.third >= minimumScore) {
            best.first
        } else {
            null
        }
    }

    private fun normalizeName(value: String): String {
        return value
            .lowercase()
            .replace("&", "and")
            .replace(Regex("[^a-z0-9]+"), "")
    }

    private fun tokenize(value: String): Set<String> {
        val stopWords = setOf("the", "and", "with", "for", "to", "a", "an", "in", "on", "of")
        return value
            .lowercase()
            .replace("&", " and ")
            .split(Regex("[^a-z0-9]+"))
            .map { token ->
                if (token.endsWith("s") && token.length > 3) token.dropLast(1) else token
            }
            .filter { it.isNotBlank() && it !in stopWords }
            .toSet()
    }
}