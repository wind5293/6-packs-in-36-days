package com.example.fitflow.data

import android.content.Context
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.FitnessGoal
import com.google.gson.Gson
import java.io.InputStreamReader


class PlanRepository(private val context: Context) {

    private val gson = Gson()

    fun loadPlan(planId: String): List<DayPlan>? {
        val path = "plans/$planId.json"
        return try {
            context.assets.open(path).use { stream ->
                InputStreamReader(stream).use { reader ->
                    val wrapper = gson.fromJson(reader, PlanFile::class.java)
                    wrapper.days
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getPlanForGoal(goal: FitnessGoal): List<DayPlan>? {
        // Map goal to a specific plan asset.
        val planId = when (goal) {
            FitnessGoal.WEIGHT_LOSS -> "weight-loss"
            FitnessGoal.MUSCLE_GAIN -> "muscle-gain"
            FitnessGoal.ENDURANCE -> "endurance"
            FitnessGoal.MAINTENANCE -> "maintenance"
        }

        return planId?.let { loadPlan(it) }
    }

    private data class PlanFile(
        val planId: String,
        val title: String,
        val days: List<DayPlan>
    )
}
