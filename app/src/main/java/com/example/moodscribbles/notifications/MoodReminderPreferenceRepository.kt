package com.example.moodscribbles.notifications

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class MoodReminderSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
)

class MoodReminderPreferenceRepository(
    private val dataStore: DataStore<Preferences>,
) {

    private val enabledKey = booleanPreferencesKey("reminder_enabled")
    private val hourKey = intPreferencesKey("reminder_hour")
    private val minuteKey = intPreferencesKey("reminder_minute")

    val reminderSettings: Flow<MoodReminderSettings> = dataStore.data.map { prefs ->
        MoodReminderSettings(
            enabled = prefs[enabledKey] ?: DEFAULT_ENABLED,
            hour = prefs[hourKey] ?: DEFAULT_HOUR,
            minute = prefs[minuteKey] ?: DEFAULT_MINUTE,
        )
    }

    suspend fun currentSettings(): MoodReminderSettings = reminderSettings.first()

    suspend fun setReminderEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[enabledKey] = enabled
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        require(hour in 0..23 && minute in 0..59) {
            "Invalid reminder time: $hour:$minute"
        }
        dataStore.edit { prefs ->
            prefs[hourKey] = hour
            prefs[minuteKey] = minute
        }
    }

    companion object {
        const val DEFAULT_HOUR = 20
        const val DEFAULT_MINUTE = 0
        const val DEFAULT_ENABLED = false
    }
}
