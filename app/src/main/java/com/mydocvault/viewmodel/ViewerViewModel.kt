package com.mydocvault.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydocvault.data.entity.FolderEntity
import com.mydocvault.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val repository: VaultRepository
) : ViewModel() {

    val folders: StateFlow<List<FolderEntity>> = repository.allFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _savedDocumentId = MutableStateFlow<Long?>(null)
    val savedDocumentId: StateFlow<Long?> = _savedDocumentId.asStateFlow()

    fun createFolder(name: String, onCreated: (FolderEntity) -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newId = repository.createFolder(name.trim(), null)
            val folder = repository.getFolder(newId)
            if (folder != null) {
                onCreated(folder)
            }
        }
    }

    fun saveToVault(
        uri: Uri,
        displayName: String,
        folderId: Long?,
        notes: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val fileType = com.mydocvault.utils.FileType.fromFileName(displayName).raw
                val docId = repository.importDocument(
                    uri = uri,
                    displayName = displayName,
                    folderId = folderId,
                    fileType = fileType,
                    notes = notes
                )
                _savedDocumentId.value = docId
                _isSaving.value = false
                onSuccess()
            } catch (_: Exception) {
                _isSaving.value = false
            }
        }
    }
}
