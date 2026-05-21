package com.example.moodscribbles.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.moodscribbles.domain.repository.JournalRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class MoodReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val journalRepository: JournalRepository by inject()
    private val reminderPrefsRepository: MoodReminderPreferenceRepository by inject()

    override suspend fun doWork(): Result {
        val settings = reminderPrefsRepository.currentSettings()
        
        if (!settings.enabled) {
            return Result.success()
        }
        
        if (!MoodReminderPermissionHelper.hasNotificationPermission(applicationContext)) {
            return Result.success()
        }

        val lastEntry = journalRepository.getEntries().firstOrNull()
        val today = LocalDate.now()
        
        if (lastEntry != null) {
            val daysSinceLastEntry = ChronoUnit.DAYS.between(lastEntry.date, today)
            
            when {
                daysSinceLastEntry >= 14 -> {

                    MoodNotificationManager(applicationContext).showReminder(MoodNotificationManager.ReminderType.AUTO_OFF)
                    reminderPrefsRepository.setReminderEnabled(false)
                }
                daysSinceLastEntry >= 2 -> {

                    MoodNotificationManager(applicationContext).showReminder(MoodNotificationManager.ReminderType.INACTIVE)
                }
                else -> {

                    MoodNotificationManager(applicationContext).showReminder(MoodNotificationManager.ReminderType.DAILY)
                }
            }
        } else {
            // No entries at all: Use daily messages
            MoodNotificationManager(applicationContext).showReminder(MoodNotificationManager.ReminderType.DAILY)
        }

        return Result.success()
    }
}
