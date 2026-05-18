package com.example.moodscribbles.data.preferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BiometricPreferenceRepository(
    private val dataStore: DataStore<Preferences>,
) {

    private val biometricLockEnabledKey = booleanPreferencesKey("biometric_lock_enabled")

    val isBiometricLockEnabled: Flow<Boolean> = dataStore.data.map { prefs -> prefs[biometricLockEnabledKey] ?: false 
    }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> 
            prefs[biometricLockEnabledKey] = enabled
        }
    }
}