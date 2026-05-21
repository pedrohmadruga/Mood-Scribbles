package com.example.moodscribbles

import android.app.Application
import com.example.moodscribbles.core.di.appModule
import com.example.moodscribbles.notifications.MoodReminderPreferenceRepository
import com.example.moodscribbles.notifications.MoodReminderScheduler
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MoodScribblesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MoodScribblesApplication)
            modules(appModule)
        }
        bootstrapMoodReminders()
    }

    private fun bootstrapMoodReminders() {
        val repository = GlobalContext.get().get<MoodReminderPreferenceRepository>()
        runBlocking {
            val settings = repository.currentSettings()
            if (settings.enabled) {
                MoodReminderScheduler.scheduleDailyReminder(
                    context = this@MoodScribblesApplication,
                    hour = settings.hour,
                    minute = settings.minute,
                )
            }
        }
    }
}
