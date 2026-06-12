package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.util.ReminderManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule notification alarm if enabled in preferences
            val prefs = context.getSharedPreferences("cinelingo_prefs", Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean("reminder_enabled", false)
            if (enabled) {
                val hour = prefs.getInt("reminder_hour", 9)
                val minute = prefs.getInt("reminder_minute", 0)
                ReminderManager.scheduleDailyReminder(context, hour, minute)
            }
            return
        }

        // Fetch word count asynchronously and show warning/encouraging notification
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cinelingo_db"
                )
                .fallbackToDestructiveMigration()
                .build()

                val savedWords = database.savedWordDao().getAllWordsList()
                database.close()

                showNotification(context, savedWords.size)
            } catch (e: Exception) {
                showNotification(context, 0)
            }
        }
    }

    private fun showNotification(context: Context, savedCount: Int) {
        val channelId = "daily_saved_words_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Өдрийн давтлага сануулагч",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Шинэ үг цээжлэх, өмнөх үгээ бататгах өдрийн сануулга"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create an intent to open MainActivity and navigate to saved words
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("target_tab", "saved")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            100,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🎬 CineLingo - Өдрийн давтлага"
        val contentText = if (savedCount > 0) {
            "Танд давтах шаардлагатай $savedCount карт байна. Одоо хамтдаа бататгая!"
        } else {
            "Өнөөдөр шинэ кино үзэж, сонирхолтой үгсийг хадгалан цээжлээрэй!"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.star_on) // Small icon
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(13579, notification)
    }
}
