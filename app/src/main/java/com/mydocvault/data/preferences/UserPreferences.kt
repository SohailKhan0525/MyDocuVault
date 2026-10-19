package com.mydocvault.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mydocvault.utils.AndroidKeystoreCipher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
    private val storageLocationKey = stringPreferencesKey("storage_location")

    companion object {
        const val STORAGE_INTERNAL = "internal"
        const val STORAGE_DOCUMENTS = "documents"
        const val STORAGE_DOWNLOADS = "downloads"
    }

    val storageLocationFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[storageLocationKey] ?: STORAGE_INTERNAL
    }

    // Legacy plaintext PINs are read temporarily so existing installs can migrate in-place.
    val pinFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[pinKey]?.let(AndroidKeystoreCipher::decryptStoredValue)
    }

    val biometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[biometricEnabledKey] ?: false
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[onboardingCompletedKey] ?: false
    }

    suspend fun setStorageLocation(location: String) {
        context.dataStore.edit { prefs -> prefs[storageLocationKey] = location }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs -> prefs[onboardingCompletedKey] = completed }
    }

    suspend fun setPin(pin: String) {
        require(pin.length == 4 && pin.all(Char::isDigit)) { "PIN must contain exactly four digits." }
        val encrypted = AndroidKeystoreCipher.encrypt(pin)
        context.dataStore.edit { prefs -> prefs[pinKey] = encrypted }
    }

    /**
     * Encrypts the previous plaintext PIN without changing it. The compare-before-write
     * avoids overwriting a newer PIN if the user changes it while migration is running.
     */
    suspend fun migrateLegacyPinIfNeeded() {
        val current = context.dataStore.data.first()[pinKey] ?: return
        if (AndroidKeystoreCipher.isEncrypted(current)) return
        val encrypted = AndroidKeystoreCipher.encrypt(current)
        context.dataStore.edit { prefs ->
            if (prefs[pinKey] == current) prefs[pinKey] = encrypted
        }
    }

    suspend fun clearPin() {
        context.dataStore.edit { prefs -> prefs.remove(pinKey) }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[biometricEnabledKey] = enabled }
    }
}