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

    fun getPlanForGoal(goal: FitnessGoal, equipment: String? = null): List<DayPlan>? {
        // Map goal + equipment to a specific plan asset. Equipment values expected: "bodyweight", "minimal", "gym"
        val planId = when (goal) {
            FitnessGoal.WEIGHT_LOSS -> when (equipment) {
                "bodyweight" -> "jefit-bodyweight-weightloss"
                "gym", "full" , "fullprotocol" -> "jefit-fullprotocol-weightloss"
                else -> "jefit-month1"
            }
            FitnessGoal.MUSCLE_GAIN -> when (equipment) {
                "gym", "full", "fullprotocol" -> "jefit-fullprotocol-muscle"
                else -> "jefit-month1"
            }
            FitnessGoal.ENDURANCE -> "jefit-endurance"
            FitnessGoal.MAINTENANCE -> "jefit-maintenance"
            FitnessGoal.ABS_CORE_STRENGTH -> "jefit-abs-core"
        }

        return planId?.let { loadPlan(it) }
    }

    private data class PlanFile(
        val planId: String,
        val title: String,
        val days: List<DayPlan>
    )
}
