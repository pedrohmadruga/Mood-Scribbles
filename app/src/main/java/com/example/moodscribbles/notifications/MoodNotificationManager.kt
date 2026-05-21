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
import kotlin.random.Random

class MoodNotificationManager(
    private val context: Context,
) {

    private val notificationManager = NotificationManagerCompat.from(context)

    enum class ReminderType {
        DAILY, INACTIVE, AUTO_OFF
    }

    fun showReminder(type: ReminderType) {
        ensureNotificationChannel()

        val (title, text) = getMessage(type)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(createContentPendingIntent())
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getMessage(type: ReminderType): Pair<String, String> {
        return when (type) {
            ReminderType.AUTO_OFF -> {
                context.getString(R.string.notif_auto_off_title) to context.getString(R.string.notif_auto_off_text)
            }
            ReminderType.INACTIVE -> {
                val titles = listOf(
                    R.string.notif_inactive_title_1,
                    R.string.notif_inactive_title_2,
                    R.string.notif_inactive_title_3
                )
                val texts = listOf(
                    R.string.notif_inactive_text_1,
                    R.string.notif_inactive_text_2,
                    R.string.notif_inactive_text_3
                )
                context.getString(titles.random()) to context.getString(texts.random())
            }
            ReminderType.DAILY -> {
                val titles = listOf(
                    R.string.notif_daily_title_1,
                    R.string.notif_daily_title_2,
                    R.string.notif_daily_title_3
                )
                val texts = listOf(
                    R.string.notif_daily_text_1,
                    R.string.notif_daily_text_2,
                    R.string.notif_daily_text_3
                )
                context.getString(titles.random()) to context.getString(texts.random())
            }
        }
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
