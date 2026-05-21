package com.example.moodscribbles.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.moodscribbles.R
import com.example.moodscribbles.ui.MainActivity
import com.example.moodscribbles.ui.prototype.AppRoutes

class MoodNotificationManager(
    private val context: Context,
) {

    private val notificationManager = NotificationManagerCompat.from(context)

    fun showDailyReminder() {
        ensureNotificationChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_reminder_title))
            .setContentText(context.getString(R.string.notification_reminder_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(createContentPendingIntent())
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    private fun createContentPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MoodReminderIntentExtras.EXTRA_NAV_ROUTE, AppRoutes.FUNCTIONAL_JOURNAL)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, REQUEST_CODE_OPEN_JOURNAL, intent, flags)
    }

    companion object {
        const val CHANNEL_ID = "mood_reminder_channel"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_CODE_OPEN_JOURNAL = 1001
    }
}
