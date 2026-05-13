package com.example.moodscribbles.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val APP_SETTINGS_NAME = "app_settings"

val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = APP_SETTINGS_NAME)
