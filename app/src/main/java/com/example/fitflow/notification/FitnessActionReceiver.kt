package com.example.fitflow.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FitnessActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "ACTION_TOGGLE_WATER" -> FitnessNotificationService.toggleWaterExpand(context)
            "ACTION_WATER_MINUS"  -> FitnessNotificationService.adjustInputMl(context, -50)
            "ACTION_WATER_PLUS"   -> FitnessNotificationService.adjustInputMl(context, +50)
            "ACTION_ADD_WATER"    -> FitnessNotificationService.addWater(context)
        }
    }
}