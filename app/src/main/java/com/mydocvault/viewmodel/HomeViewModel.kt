package com.mydocvault.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydocvault.data.entity.FolderEntity
import com.mydocvault.data.repository.VaultRepository
import com.mydocvault.utils.GlobalErrorBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: VaultRepository
) : ViewModel() {
    val folders: StateFlow<List<FolderEntity>> = repository.foldersByParent(null)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createFolder(name: String) {
        viewModelScope.launch {
            try {
                repository.createFolder(name, null)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Create folder failed")
            }
        }
    }

    fun renameFolder(folderId: Long, name: String) {
        viewModelScope.launch {
            try {
                repository.renameFolder(folderId, name)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Rename folder failed")
            }
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteFolderRecursive(folderId)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Delete folder failed")
            }
        }
    }

    fun importDocument(uri: android.net.Uri, name: String, type: String) {
        viewModelScope.launch {
            try {
                repository.importDocument(uri, name, null, type)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Import document failed")
            }
        }
    }
}
