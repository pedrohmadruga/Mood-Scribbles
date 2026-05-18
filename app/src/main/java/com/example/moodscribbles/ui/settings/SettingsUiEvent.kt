package com.example.moodscribbles.ui.settings

sealed interface SettingsUiEvent {
    data class ShowMessage(val message: String) : SettingsUiEvent
}