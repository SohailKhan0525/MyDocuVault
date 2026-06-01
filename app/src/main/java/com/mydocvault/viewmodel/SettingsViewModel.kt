package com.mydocvault.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydocvault.data.preferences.UserPreferences
import com.mydocvault.utils.BackupManager
import com.mydocvault.utils.GlobalErrorBus
import com.mydocvault.utils.UpdateChecker
import com.mydocvault.utils.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferences,
    private val updateChecker: UpdateChecker,
    private val backupManager: BackupManager
) : ViewModel() {
    val biometricEnabled: StateFlow<Boolean> = prefs.biometricEnabledFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking

    private val _downloadProgress = MutableStateFlow<Int?>(null)
    val downloadProgress: StateFlow<Int?> = _downloadProgress

    // Backup / Restore state
    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring

    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage

    private val _restartApp = MutableStateFlow(false)
    val restartApp: StateFlow<Boolean> = _restartApp

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setBiometricEnabled(enabled)
        }
    }

    fun checkForUpdate(context: Context, owner: String, repo: String) {
        viewModelScope.launch {
            _isChecking.value = true
            _error.value = null
            try {
                val info = updateChecker.checkForUpdate(context, owner, repo)
                _updateInfo.value = info
                if (info == null) {
                    _error.value = "You're up to date! No newer version found."
                }
            } catch (e: Exception) {
                handleError(e) { _error.value = it }
            } finally {
                _isChecking.value = false
            }
        }
    }

    fun downloadAndInstall(context: Context, apkUrl: String) {
        viewModelScope.launch {
            try {
                _downloadProgress.value = 0
                val file = updateChecker.downloadApk(context, apkUrl) { percent ->
                    _downloadProgress.value = percent
                }
                updateChecker.startInstall(context, file)
            } catch (e: Exception) {
                handleError(e) { _error.value = it }
            } finally {
                _downloadProgress.value = null
            }
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _isBackingUp.value = true
            try {
                val file = backupManager.createBackup()
                _backupMessage.value = if (file != null) {
                    "Backup saved.\nLocation: ${file.absolutePath}"
                } else {
                    "Backup failed. Please try again."
                }
            } catch (e: Exception) {
                handleError(e) { _backupMessage.value = it }
            } finally {
                _isBackingUp.value = false
            }
        }
    }

    fun restoreBackup(zipFile: File) {
        viewModelScope.launch {
            _isRestoring.value = true
            try {
                val success = backupManager.restoreBackup(zipFile)
                _backupMessage.value = if (success) {
                    "Restore successful. The app will now restart."
                } else {
                    "Restore failed. The selected file may be invalid."
                }
                if (success) {
                    _restartApp.value = true
                }
            } catch (e: Exception) {
                handleError(e) { _backupMessage.value = it }
            } finally {
                _isRestoring.value = false
            }
        }
    }

    fun clearUpdateInfo() {
        _updateInfo.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun clearBackupMessage() {
        _backupMessage.value = null
    }

    fun setBackupMessage(message: String) {
        _backupMessage.value = message
    }

    private fun handleError(exception: Exception, sink: (String) -> Unit) {
        val fullError = exception.stackTraceToString()
        sink(fullError)
        GlobalErrorBus.emit(fullError)
    }
}
