package com.example.moodscribbles.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object MoodReminderScheduler {

    const val UNIQUE_WORK_NAME = "mood_daily_reminder"

    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<MoodReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayUntilNextOccurrenceMillis(hour, minute), TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    internal fun delayUntilNextOccurrenceMillis(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        var target = now.with(LocalTime.of(hour, minute))
        if (!target.isAfter(now)) {
            target = target.plusDays(1)
        }
        return Duration.between(now, target).toMillis()
    }
}
