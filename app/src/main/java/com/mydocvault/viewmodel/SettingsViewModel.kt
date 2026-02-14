package com.mydocvault.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydocvault.data.preferences.UserPreferences
import com.mydocvault.utils.UpdateChecker
import com.mydocvault.utils.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferences,
    private val updateChecker: UpdateChecker
) : ViewModel() {
    val biometricEnabled: StateFlow<Boolean> = prefs.biometricEnabledFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setBiometricEnabled(enabled)
        }
    }

    fun checkForUpdate(context: Context, owner: String, repo: String) {
        viewModelScope.launch {
            _isChecking.value = true
            _updateInfo.value = updateChecker.checkForUpdate(context, owner, repo)
            _isChecking.value = false
        }
    }

    fun downloadAndInstall(context: Context, apkUrl: String) {
        viewModelScope.launch {
            val file = updateChecker.downloadApk(context, apkUrl)
            updateChecker.startInstall(context, file)
        }
    }
}
