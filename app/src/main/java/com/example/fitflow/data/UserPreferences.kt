package com.example.fitflow.data

import android.content.Context
import com.example.fitflow.data.model.DailyHealthMetrics
import com.example.fitflow.data.model.FitnessGoal
import com.example.fitflow.data.model.StepSource
import com.example.fitflow.data.model.UserProfile
import com.example.fitflow.domain.calculateBmi
import com.example.fitflow.domain.getBmiCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.fitflow.data.model.WorkoutExercise
import java.time.LocalDate

class UserPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("fitflow_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val KEY_HEIGHT = "height"
        const val KEY_WEIGHT = "weight"
        const val KEY_BIRTH_YEAR = "birth_year"
        const val KEY_TARGET_WEIGHT = "target_weight"
        const val KEY_IS_ONBOARDED = "is_onboarded"
        const val KEY_COMPLETED_DAYS = "completed_days"
        const val KEY_GOAL           = "goal"
        const val KEY_EQUIPMENT      = "equipment"
        const val KEY_START_DATE     = "start_date"
        const val KEY_WORKOUT_TIME   = "workout_time"
        const val KEY_WEIGHT_HISTORY = "weight_history"
        const val KEY_HEALTH_HISTORY = "health_history"
        const val KEY_STEP_BASELINE_DAY = "step_baseline_day"
        const val KEY_STEP_BASELINE_VALUE = "step_baseline_value"
        const val KEY_STEP_SENSOR_ENABLED = "step_sensor_enabled"

        const val KEY_SETTING_BG_MUSIC_ENABLED = "setting_bg_music_enabled"
        const val KEY_SETTING_BG_MUSIC_VOLUME = "setting_bg_music_volume"
        const val KEY_SETTING_VOICE_GUIDE_ENABLED = "setting_voice_guide_enabled"
        const val KEY_SETTING_COACH_NAME = "setting_coach_name"
        const val KEY_SETTING_COACH_VOLUME = "setting_coach_volume"
        const val KEY_SETTING_SOUND_EFFECT_ENABLED = "setting_sound_effect_enabled"
        const val KEY_SETTING_AUTO_COUNTING = "setting_auto_counting"
        const val KEY_SETTING_REST_TIMER = "setting_rest_timer"
        const val KEY_SETTING_COUNTDOWN = "setting_countdown"
        const val KEY_HYBRID_GIF_CACHE_READY = "hybrid_gif_cache_ready"
        const val KEY_HYBRID_GIF_CACHE_SIGNATURE = "hybrid_gif_cache_signature"

        const val HISTORY_MAX_DAYS = 90
    }

    fun saveUserProfile(
        selectedGoal: FitnessGoal,
        height: Float,
        weight: Float,
        birthYear: Int,
        targetWeight: Float,
        workoutTime: String,
        equipment: String = "bodyweight"
    ) {
        prefs.edit()
            .putString(KEY_GOAL, selectedGoal.name)
            .putString(KEY_EQUIPMENT, equipment)
            .putFloat(KEY_HEIGHT, height)
            .putFloat(KEY_WEIGHT, weight)
            .putInt(KEY_BIRTH_YEAR, birthYear)
            .putFloat(KEY_TARGET_WEIGHT, targetWeight)
            .putLong(KEY_START_DATE, LocalDate.now().toEpochDay())
            .putString(KEY_WORKOUT_TIME, workoutTime)
            .apply()

            upsertWeightRecord(LocalDate.now(), weight)
    }

    fun saveEquipment(equipment: String) {
        prefs.edit().putString(KEY_EQUIPMENT, equipment).apply()
    }

    fun getStartDate(): LocalDate? {
        val epochDay = prefs.getLong(KEY_START_DATE, -1L)
        return if (epochDay == -1L) null else LocalDate.ofEpochDay(epochDay)
    }

    fun saveGoal(goal: FitnessGoal) {
        prefs.edit().putString(KEY_GOAL, goal.name).apply()
    }

    fun getUserProfile(): UserProfile? {
        val height = prefs.getFloat(KEY_HEIGHT, 0f)
        val weight = prefs.getFloat(KEY_WEIGHT, 0f)
        val birthYear = prefs.getInt(KEY_BIRTH_YEAR, 0)
        val targetWeight = prefs.getFloat(KEY_TARGET_WEIGHT, 0f)

        if (height == 0f || weight == 0f) {
            return null
        }

        val bmi = calculateBmi(height, weight)
        val bmiCategory = getBmiCategory(bmi)
        val goal = try {
            FitnessGoal.valueOf(
                prefs.getString(KEY_GOAL, FitnessGoal.WEIGHT_LOSS.name)
                    ?: FitnessGoal.WEIGHT_LOSS.name
            )
        } catch (e: IllegalArgumentException) {
            FitnessGoal.WEIGHT_LOSS
        }
        val equipment = prefs.getString(KEY_EQUIPMENT, "bodyweight") ?: "bodyweight"

        return UserProfile(height, weight, birthYear, targetWeight, bmi, bmiCategory, goal, equipment)
    }

    fun setOnboarded(value: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ONBOARDED, value).apply()
    }

    fun isOnboarded(): Boolean = prefs.getBoolean(KEY_IS_ONBOARDED, false)

    fun saveCompletedDays(days: Set<Int>) {
        prefs.edit().putString(KEY_COMPLETED_DAYS, days.joinToString(",")).apply()
    }

    fun getCompletedDays(): Set<Int> {
        val raw = prefs.getString(KEY_COMPLETED_DAYS, "") ?: return emptySet()
        if (raw.isEmpty()) return emptySet()
        return raw.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun recordWeight(weight: Float, date: LocalDate = LocalDate.now()) {
        prefs.edit().putFloat(KEY_WEIGHT, weight).apply()
        upsertWeightRecord(date, weight)
    }

    fun getWeightHistory(): List<Pair<LocalDate, Float>> {
        val raw = prefs.getString(KEY_WEIGHT_HISTORY, "") ?: return emptyList()
        if (raw.isBlank()) return emptyList()

        return raw.split(";")
            .mapNotNull { token ->
                val parts = token.split(":")
                if (parts.size != 2) return@mapNotNull null
                val epochDay = parts[0].toLongOrNull() ?: return@mapNotNull null
                val weight = parts[1].toFloatOrNull() ?: return@mapNotNull null
                LocalDate.ofEpochDay(epochDay) to weight
            }
            .sortedBy { it.first }
    }

    private fun upsertWeightRecord(date: LocalDate, weight: Float) {
        val mutable = getWeightHistory().toMutableList()
        val idx = mutable.indexOfFirst { it.first == date }
        if (idx >= 0) {
            mutable[idx] = date to weight
        } else {
            mutable.add(date to weight)
        }

        val encoded = mutable
            .sortedBy { it.first }
            .joinToString(";") { "${it.first.toEpochDay()}:${it.second}" }

        prefs.edit().putString(KEY_WEIGHT_HISTORY, encoded).apply()
    }

    fun getTodayHealthMetrics(defaultWaterGoalMl: Int): DailyHealthMetrics {
        trimHealthHistory(HISTORY_MAX_DAYS)
        val today = LocalDate.now()
        val history = getHealthHistoryMap()
        return history[today] ?: DailyHealthMetrics(
            date = today,
            steps = 0,
            waterIntakeMl = 0,
            waterGoalMl = defaultWaterGoalMl,
            stepSource = StepSource.MANUAL
        )
    }

    fun getHealthMetricsHistory(days: Int = HISTORY_MAX_DAYS): List<DailyHealthMetrics> {
        trimHealthHistory(days)
        return getHealthHistoryMap().values.sortedBy { it.date }
    }

    fun addWater(amountMl: Int, defaultWaterGoalMl: Int) {
        if (amountMl <= 0) return
        val today = LocalDate.now()
        val current = getTodayHealthMetrics(defaultWaterGoalMl)
        val updated = current.copy(waterIntakeMl = (current.waterIntakeMl + amountMl).coerceAtLeast(0))
        upsertHealthMetrics(today, updated)
    }

    fun setWaterGoal(goalMl: Int, defaultWaterGoalMl: Int) {
        if (goalMl <= 0) return
        val today = LocalDate.now()
        val current = getTodayHealthMetrics(defaultWaterGoalMl)
        val updated = current.copy(waterGoalMl = goalMl)
        upsertHealthMetrics(today, updated)
    }

    fun setTodaySteps(steps: Int, defaultWaterGoalMl: Int, source: StepSource) {
        val today = LocalDate.now()
        val current = getTodayHealthMetrics(defaultWaterGoalMl)
        val updated = current.copy(steps = steps.coerceAtLeast(0), stepSource = source)
        upsertHealthMetrics(today, updated)
    }

    fun incrementTodaySteps(delta: Int, defaultWaterGoalMl: Int, source: StepSource) {
        if (delta <= 0) return
        val today = LocalDate.now()
        val current = getTodayHealthMetrics(defaultWaterGoalMl)
        val updated = current.copy(
            steps = (current.steps + delta).coerceAtLeast(0),
            stepSource = source
        )
        upsertHealthMetrics(today, updated)
    }

    fun getStepBaselineDay(): LocalDate? {
        val epoch = prefs.getLong(KEY_STEP_BASELINE_DAY, -1L)
        return if (epoch == -1L) null else LocalDate.ofEpochDay(epoch)
    }

    fun getStepBaselineValue(): Int = prefs.getInt(KEY_STEP_BASELINE_VALUE, -1)

    fun setStepBaseline(day: LocalDate, baseline: Int) {
        prefs.edit()
            .putLong(KEY_STEP_BASELINE_DAY, day.toEpochDay())
            .putInt(KEY_STEP_BASELINE_VALUE, baseline)
            .apply()
    }

    fun setStepSensorEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STEP_SENSOR_ENABLED, enabled).apply()
    }

    fun isStepSensorEnabled(): Boolean = prefs.getBoolean(KEY_STEP_SENSOR_ENABLED, false)

    private fun upsertHealthMetrics(day: LocalDate, metrics: DailyHealthMetrics) {
        val map = getHealthHistoryMap().toMutableMap()
        map[day] = metrics.copy(date = day)
        saveHealthHistoryMap(map)
    }

    private fun trimHealthHistory(maxDays: Int) {
        val map = getHealthHistoryMap()
        if (map.size <= maxDays) return
        val trimmed = map.entries
            .sortedBy { it.key }
            .takeLast(maxDays)
            .associate { it.key to it.value }
        saveHealthHistoryMap(trimmed)
    }

    private fun getHealthHistoryMap(): Map<LocalDate, DailyHealthMetrics> {
        val raw = prefs.getString(KEY_HEALTH_HISTORY, "") ?: return emptyMap()
        if (raw.isBlank()) return emptyMap()

        return raw.split(";")
            .mapNotNull { token ->
                // format: epochDay,steps,waterIntake,waterGoal,source
                val parts = token.split(",")
                if (parts.size < 5) return@mapNotNull null

                val epochDay = parts[0].toLongOrNull() ?: return@mapNotNull null
                val steps = parts[1].toIntOrNull() ?: return@mapNotNull null
                val waterIntake = parts[2].toIntOrNull() ?: return@mapNotNull null
                val waterGoal = parts[3].toIntOrNull() ?: return@mapNotNull null
                val source = try {
                    StepSource.valueOf(parts[4])
                } catch (_: IllegalArgumentException) {
                    StepSource.MANUAL
                }

                val date = LocalDate.ofEpochDay(epochDay)
                date to DailyHealthMetrics(
                    date = date,
                    steps = steps,
                    waterIntakeMl = waterIntake,
                    waterGoalMl = waterGoal,
                    stepSource = source
                )
            }
            .toMap()
    }

    private fun saveHealthHistoryMap(map: Map<LocalDate, DailyHealthMetrics>) {
        val encoded = map.values
            .sortedBy { it.date }
            .joinToString(";") {
                "${it.date.toEpochDay()},${it.steps},${it.waterIntakeMl},${it.waterGoalMl},${it.stepSource.name}"
            }
        prefs.edit().putString(KEY_HEALTH_HISTORY, encoded).apply()
    }

    fun saveCustomDayPlan(dayNumber: Int, exercises: List<WorkoutExercise>) {
        val json = gson.toJson(exercises)
        prefs.edit().putString("custom_day_plan_$dayNumber", json).apply()
    }

    fun getCustomDayPlan(dayNumber: Int): List<WorkoutExercise>? {
        val json = prefs.getString("custom_day_plan_$dayNumber", null) ?: return null
        return try {
            val type = object : TypeToken<List<WorkoutExercise>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    fun clearCustomDayPlans() {
        val editor = prefs.edit()
        prefs.all.keys.forEach { key ->
            if (key.startsWith("custom_day_plan_")) {
                editor.remove(key)
            }
        }
        editor.apply()
    }

    fun isBgMusicEnabled(): Boolean = prefs.getBoolean(KEY_SETTING_BG_MUSIC_ENABLED, true)
    fun setBgMusicEnabled(value: Boolean) = prefs.edit().putBoolean(KEY_SETTING_BG_MUSIC_ENABLED, value).apply()

    fun getBgMusicVolume(): Float = prefs.getFloat(KEY_SETTING_BG_MUSIC_VOLUME, 0.5f)
    fun setBgMusicVolume(value: Float) = prefs.edit().putFloat(KEY_SETTING_BG_MUSIC_VOLUME, value).apply()

    fun isVoiceGuideEnabled(): Boolean = prefs.getBoolean(KEY_SETTING_VOICE_GUIDE_ENABLED, true)
    fun setVoiceGuideEnabled(value: Boolean) = prefs.edit().putBoolean(KEY_SETTING_VOICE_GUIDE_ENABLED, value).apply()

    fun getCoachName(): String = prefs.getString(KEY_SETTING_COACH_NAME, "James") ?: "James"
    fun setCoachName(value: String) = prefs.edit().putString(KEY_SETTING_COACH_NAME, value).apply()

    fun getCoachVolume(): Float = prefs.getFloat(KEY_SETTING_COACH_VOLUME, 0.8f)
    fun setCoachVolume(value: Float) = prefs.edit().putFloat(KEY_SETTING_COACH_VOLUME, value).apply()

    fun isSoundEffectEnabled(): Boolean = prefs.getBoolean(KEY_SETTING_SOUND_EFFECT_ENABLED, true)
    fun setSoundEffectEnabled(value: Boolean) = prefs.edit().putBoolean(KEY_SETTING_SOUND_EFFECT_ENABLED, value).apply()

    fun getAutoCounting(): String = prefs.getString(KEY_SETTING_AUTO_COUNTING, "Off") ?: "Off"
    fun setAutoCounting(value: String) = prefs.edit().putString(KEY_SETTING_AUTO_COUNTING, value).apply()

    fun getRestTimer(): String = prefs.getString(KEY_SETTING_REST_TIMER, "30s") ?: "30s"
    fun setRestTimer(value: String) = prefs.edit().putString(KEY_SETTING_REST_TIMER, value).apply()

    fun getCountdown(): String = prefs.getString(KEY_SETTING_COUNTDOWN, "5s") ?: "5s"
    fun setCountdown(value: String) = prefs.edit().putString(KEY_SETTING_COUNTDOWN, value).apply()

    fun markHybridGifCachePending(signature: String) {
        prefs.edit()
            .putBoolean(KEY_HYBRID_GIF_CACHE_READY, false)
            .putString(KEY_HYBRID_GIF_CACHE_SIGNATURE, signature)
            .apply()
    }

    fun markHybridGifCacheReady(signature: String) {
        prefs.edit()
            .putBoolean(KEY_HYBRID_GIF_CACHE_READY, true)
            .putString(KEY_HYBRID_GIF_CACHE_SIGNATURE, signature)
            .apply()
    }

    fun isHybridGifCacheReady(): Boolean = prefs.getBoolean(KEY_HYBRID_GIF_CACHE_READY, false)

    fun getHybridGifCacheSignature(): String? = prefs.getString(KEY_HYBRID_GIF_CACHE_SIGNATURE, null)
}