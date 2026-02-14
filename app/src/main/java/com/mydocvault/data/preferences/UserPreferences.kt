package com.mydocvault.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mydocvault_settings")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val pinKey = stringPreferencesKey("pin")
    private val biometricEnabledKey = booleanPreferencesKey("biometric_enabled")

    val pinFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[pinKey]
    }

    val biometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[biometricEnabledKey] ?: false
    }

    suspend fun setPin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[pinKey] = pin
        }
    }

    suspend fun clearPin() {
        context.dataStore.edit { prefs ->
            prefs.remove(pinKey)
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[biometricEnabledKey] = enabled
        }
    }
}
