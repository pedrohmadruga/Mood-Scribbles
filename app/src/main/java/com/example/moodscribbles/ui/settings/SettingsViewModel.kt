package com.example.moodscribbles.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodscribbles.data.preferences.ThemeMode
import com.example.moodscribbles.data.preferences.ThemePreferenceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themePreferenceRepository: ThemePreferenceRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePreferenceRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM,
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferenceRepository.setThemeMode(mode)
        }
    }
}
