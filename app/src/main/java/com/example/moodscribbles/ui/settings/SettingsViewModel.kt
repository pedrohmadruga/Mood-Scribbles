package com.example.moodscribbles.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodscribbles.data.preferences.ThemeMode
import com.example.moodscribbles.data.preferences.ThemePreferenceRepository
import com.example.moodscribbles.notifications.MoodReminderPermissionHelper
import com.example.moodscribbles.notifications.MoodReminderPreferenceRepository
import com.example.moodscribbles.notifications.MoodReminderScheduler
import com.example.moodscribbles.notifications.MoodReminderSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themePreferenceRepository: ThemePreferenceRepository,
    private val moodReminderPreferenceRepository: MoodReminderPreferenceRepository,
    private val appContext: Context,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePreferenceRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM,
        )

    val moodReminderSettings: StateFlow<MoodReminderSettings> =
        moodReminderPreferenceRepository.reminderSettings
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MoodReminderSettings(
                    enabled = MoodReminderPreferenceRepository.DEFAULT_ENABLED,
                    hour = MoodReminderPreferenceRepository.DEFAULT_HOUR,
                    minute = MoodReminderPreferenceRepository.DEFAULT_MINUTE,
                ),
            )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferenceRepository.setThemeMode(mode)
        }
    }

    fun setReminderEnabled(enabled: Boolean, hasNotificationPermission: Boolean) {
        viewModelScope.launch {
            if (enabled && !hasNotificationPermission) {
                return@launch
            }
            moodReminderPreferenceRepository.setReminderEnabled(enabled)
            if (enabled) {
                val settings = moodReminderPreferenceRepository.currentSettings()
                MoodReminderScheduler.scheduleDailyReminder(
                    context = appContext,
                    hour = settings.hour,
                    minute = settings.minute,
                )
            } else {
                MoodReminderScheduler.cancelDailyReminder(appContext)
            }
        }
    }

    fun applyReminderEnabledAfterPermissionGranted() {
        if (!MoodReminderPermissionHelper.hasNotificationPermission(appContext)) {
            return
        }
        setReminderEnabled(enabled = true, hasNotificationPermission = true)
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            moodReminderPreferenceRepository.setReminderTime(hour, minute)
            val settings = moodReminderPreferenceRepository.currentSettings()
            if (settings.enabled) {
                MoodReminderScheduler.scheduleDailyReminder(
                    context = appContext,
                    hour = hour,
                    minute = minute,
                )
            }
        }
    }
}
