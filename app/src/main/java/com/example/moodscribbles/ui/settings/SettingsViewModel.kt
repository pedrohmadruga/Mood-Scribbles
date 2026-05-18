package com.example.moodscribbles.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodscribbles.data.preferences.ThemeMode
import com.example.moodscribbles.data.preferences.ThemePreferenceRepository
import com.example.moodscribbles.data.preferences.BiometricPreferenceRepository
import com.example.moodscribbles.data.security.BiometricAuthManager
import com.example.moodscribbles.data.security.BiometricAvailability
import androidx.fragment.app.FragmentActivity
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
    

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<SettingsUiEvent> = _uiEvent.asSharedFlow()


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
            onCancel = { /* Switch volta sozinho: Flow ainda é false */ },
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferenceRepository.setThemeMode(mode)
        }
    }
}
