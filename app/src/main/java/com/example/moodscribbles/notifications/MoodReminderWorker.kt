package com.example.moodscribbles.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.moodscribbles.data.preferences.appSettingsDataStore

class MoodReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repository = MoodReminderPreferenceRepository(applicationContext.appSettingsDataStore)
        val settings = repository.currentSettings()
        if (!settings.enabled) {
            return Result.success()
        }
        if (!MoodReminderPermissionHelper.hasNotificationPermission(applicationContext)) {
            return Result.success()
        }
        MoodNotificationManager(applicationContext).showDailyReminder()
        return Result.success()
    }
}
