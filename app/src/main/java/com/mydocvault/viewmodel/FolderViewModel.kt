package com.mydocvault.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydocvault.data.entity.DocumentEntity
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
class FolderViewModel @Inject constructor(
    private val repository: VaultRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val folderId: Long = savedStateHandle.get<Long>("folderId") ?: 0L

    val subfolders: StateFlow<List<FolderEntity>> = repository.foldersByParent(folderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documents: StateFlow<List<DocumentEntity>> = repository.documentsByFolder(folderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentFolder: StateFlow<FolderEntity?> = repository.folderById(folderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun createFolder(name: String) {
        viewModelScope.launch {
            try {
                repository.createFolder(name, folderId)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Create folder failed")
            }
        }
    }

    fun renameFolder(targetId: Long, name: String) {
        viewModelScope.launch {
            try {
                repository.renameFolder(targetId, name)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Rename folder failed")
            }
        }
    }

    fun deleteFolder(targetId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteFolderRecursive(targetId)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Delete folder failed")
            }
        }
    }

    fun renameDocument(documentId: Long, name: String) {
        viewModelScope.launch {
            try {
                repository.renameDocument(documentId, name)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Rename document failed")
            }
        }
    }

    fun deleteDocument(documentId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteDocument(documentId)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Delete document failed")
            }
        }
    }

    fun replaceDocument(documentId: Long, uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                repository.replaceDocument(documentId, uri)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Replace document failed")
            }
        }
    }

    fun moveDocument(documentId: Long, newFolderId: Long?) {
        viewModelScope.launch {
            try {
                repository.moveDocument(documentId, newFolderId)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Move document failed")
            }
        }
    }

    fun importDocument(uri: android.net.Uri, name: String, type: String) {
        viewModelScope.launch {
            try {
                repository.importDocument(uri, name, folderId, type)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Import document failed")
            }
        }
    }
}
