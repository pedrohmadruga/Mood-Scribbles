package com.example.moodscribbles.ui.security

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodscribbles.data.preferences.BiometricPreferenceRepository
import com.example.moodscribbles.data.security.BiometricAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppLockViewModel(
    private val biometricPreferenceRepository: BiometricPreferenceRepository,
    private val biometricAuthManager: BiometricAuthManager,
) : ViewModel() {

    val isLockEnabled: StateFlow<Boolean> =
        biometricPreferenceRepository.isBiometricLockEnabled.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    private val _isPreferenceReady = MutableStateFlow(false)
    val isPreferenceReady: StateFlow<Boolean> = _isPreferenceReady.asStateFlow()

    private val _isUnlocked = MutableStateFlow(false)

    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    init {
        viewModelScope.launch {
            biometricPreferenceRepository.isBiometricLockEnabled.collect { enabled ->
                _isPreferenceReady.value = true
                if (!enabled) {
                    _isUnlocked.value = true
                } else {
                    _isUnlocked.value = false
                }
            }
        }
    }

    fun lockIfEnabled() {
        if (isLockEnabled.value) {
            _isUnlocked.value = false
        }
    }

    fun requestUnlock(
        activity: FragmentActivity,
        title: CharSequence,
        subtitle: CharSequence,
    ) {
        if (!isLockEnabled.value) {
            _isUnlocked.value = true
            return
        }
        biometricAuthManager.authenticate(
            activity = activity,
            title = title,
            subtitle = subtitle,
            onSuccess = { _isUnlocked.value = true },
            onError = { /* continua locked */ },
            onCancel = { /* continua locked */ },
        )
    }
}