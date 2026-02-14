package com.mydocvault.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydocvault.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val prefs: UserPreferences
) : ViewModel() {
    val pinState: StateFlow<String?> = prefs.pinFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val biometricEnabled: StateFlow<Boolean> = prefs.biometricEnabledFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    fun setPin(pin: String) {
        viewModelScope.launch {
            prefs.setPin(pin)
        }
    }

    fun clearPin() {
        viewModelScope.launch {
            prefs.clearPin()
        }
    }

    fun verifyPin(input: String): Boolean {
        return input == pinState.value
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setBiometricEnabled(enabled)
        }
    }
}
