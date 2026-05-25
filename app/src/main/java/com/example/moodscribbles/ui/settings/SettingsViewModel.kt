package com.example.moodscribbles.ui.settings

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodscribbles.data.preferences.BiometricPreferenceRepository
import com.example.moodscribbles.data.preferences.ThemeMode
import com.example.moodscribbles.data.preferences.ThemePreferenceRepository
import com.example.moodscribbles.data.security.BiometricAuthManager
import com.example.moodscribbles.data.security.BiometricAvailability
import com.example.moodscribbles.notifications.MoodReminderPermissionHelper
import com.example.moodscribbles.notifications.MoodReminderPreferenceRepository
import com.example.moodscribbles.notifications.MoodReminderScheduler
import com.example.moodscribbles.notifications.MoodReminderSettings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themePreferenceRepository: ThemePreferenceRepository,
    private val biometricPreferenceRepository: BiometricPreferenceRepository,
    private val biometricAuthManager: BiometricAuthManager,
    private val moodReminderPreferenceRepository: MoodReminderPreferenceRepository,
    private val appContext: Context,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePreferenceRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM,
        )

    val isBiometricLockEnabled: StateFlow<Boolean> =
        biometricPreferenceRepository.isBiometricLockEnabled.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val canUseBiometricLock: Boolean
        get() = biometricAuthManager.getAvailability() == BiometricAvailability.AVAILABLE

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

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<SettingsUiEvent> = _uiEvent.asSharedFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferenceRepository.setThemeMode(mode)
        }
    }

    fun onBiometricLockToggle(
        wantEnabled: Boolean,
        activity: FragmentActivity,
        title: CharSequence,
        subtitle: CharSequence,
        unavailableMessage: String,
    ) {
        if (!wantEnabled) {
            viewModelScope.launch {
                biometricPreferenceRepository.setBiometricLockEnabled(false)
            }
            return
        }

        if (biometricAuthManager.getAvailability() != BiometricAvailability.AVAILABLE) {
            viewModelScope.launch {
                _uiEvent.emit(SettingsUiEvent.ShowMessage(unavailableMessage))
            }
            return
        }

        biometricAuthManager.authenticate(
            activity = activity,
            title = title,
            subtitle = subtitle,
            onSuccess = {
                viewModelScope.launch {
                    biometricPreferenceRepository.setBiometricLockEnabled(true)
                }
            },
            onError = { message ->
                viewModelScope.launch {
                    _uiEvent.emit(SettingsUiEvent.ShowMessage(message.toString()))
                }
            },
            onCancel = { /* Switch reverts automatically: Flow is still false */ },
        )
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
