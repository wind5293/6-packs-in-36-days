package com.example.fitflow.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fitflow.R

class WorkoutReminderReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("FitFlowDebug", "Receiver: Alarm triggered!")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "workout_reminder"

        val channel = NotificationChannel(
            channelId,
            "Workout Reminder",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time to Flow! 💪")
            .setContentText("Your scheduled workout session starts now. Let's get moving!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
        Log.d("FitFlowDebug", "Receiver: Notification command sent to system.")
    }
}