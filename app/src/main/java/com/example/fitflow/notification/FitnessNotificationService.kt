package com.example.fitflow.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.fitflow.R

class FitnessNotificationService : Service() {

    companion object {
        const val NOTIF_ID   = 2
        const val CHANNEL_ID = "fitness_status_channel"

        var waterCurrent  = 0
        var waterGoal     = 2000
        var inputMl       = 250
        var waterExpanded = false

        var challengeName  = "Full Body HIIT"
        var currentDay     = 1
        var totalDays      = 26
        var challengeState = "todo"       // "todo" | "done" | "rest"

        fun toggleWaterExpand(ctx: Context) {
            waterExpanded = !waterExpanded
            refresh(ctx)
        }

        fun adjustInputMl(ctx: Context, delta: Int) {
            inputMl = (inputMl + delta).coerceIn(50, 1000)
            refresh(ctx)
        }

        fun addWater(ctx: Context) {
            val userPrefs = com.example.fitflow.data.UserPreferences(ctx)
            userPrefs.addWater(inputMl, 2000)
            
            val metrics = userPrefs.getTodayHealthMetrics(2000)
            waterCurrent = metrics.waterIntakeMl
            waterGoal = metrics.waterGoalMl
            refresh(ctx)

            val intent = Intent("com.example.fitflow.WATER_UPDATED")
            intent.setPackage(ctx.packageName)
            ctx.sendBroadcast(intent)
        }

        fun refresh(ctx: Context) {
            val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIF_ID, buildNotification(ctx))
        }

        fun buildNotification(ctx: Context): Notification {
            val views = RemoteViews(ctx.packageName, R.drawable.notification_fitness)

            // Water
            views.setTextViewText(R.id.tv_water, "$waterCurrent / $waterGoal ml")
            views.setProgressBar(R.id.progress_water, waterGoal, waterCurrent, false)
            views.setTextViewText(R.id.tv_ml_input, "$inputMl ml")
            views.setViewVisibility(
                R.id.layout_water_expand,
                if (waterExpanded) View.VISIBLE else View.GONE
            )

            fun pi(action: String) = PendingIntent.getBroadcast(
                ctx,
                action.hashCode(),
                Intent(ctx, FitnessActionReceiver::class.java).apply { this.action = action },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.row_water,     pi("ACTION_TOGGLE_WATER"))
            views.setOnClickPendingIntent(R.id.btn_minus,     pi("ACTION_WATER_MINUS"))
            views.setOnClickPendingIntent(R.id.btn_plus,      pi("ACTION_WATER_PLUS"))
            views.setOnClickPendingIntent(R.id.btn_add_water, pi("ACTION_ADD_WATER"))

            // Challenge
            views.setTextViewText(R.id.tv_challenge_name, challengeName.uppercase())
            views.setTextViewText(R.id.tv_challenge_day, "Day $currentDay")
            views.setProgressBar(R.id.progress_challenge, totalDays, currentDay - 1, false)
            views.setTextViewText(R.id.tv_challenge_progress, "${currentDay - 1} / $totalDays ngày")

            when (challengeState) {
                "done" -> {
                    views.setImageViewResource(R.id.ic_status, R.drawable.ic_trophy)
                    views.setTextViewText(R.id.tv_status, "Hoàn thành")
                    views.setTextColor(R.id.tv_status, Color.parseColor("#639922"))
                    views.setInt(R.id.layout_status, "setBackgroundResource", R.drawable.badge_bg_green)
                }
                "rest" -> {
                    views.setImageViewResource(R.id.ic_status, R.drawable.ic_moon)
                    views.setTextViewText(R.id.tv_status, "Ngày nghỉ")
                    views.setTextColor(R.id.tv_status, Color.parseColor("#888780"))
                    views.setInt(R.id.layout_status, "setBackgroundResource", R.drawable.badge_bg_gray)
                }
                else -> {
                    views.setImageViewResource(R.id.ic_status, R.drawable.ic_run)
                    views.setTextViewText(R.id.tv_status, "Chưa tập")
                    views.setTextColor(R.id.tv_status, Color.parseColor("#f5651a"))
                    views.setInt(R.id.layout_status, "setBackgroundResource", R.drawable.badge_bg_orange)
                }
            }

            return NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)   // dùng cùng icon với WorkoutReminderReceiver
                .setCustomBigContentView(views)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification(this))
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fitness Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                description = "Trạng thái tập luyện và nước uống"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
