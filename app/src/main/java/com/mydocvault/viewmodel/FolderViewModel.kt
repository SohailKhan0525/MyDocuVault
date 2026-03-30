package com.mydocvault.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydocvault.data.entity.DocumentEntity
import com.mydocvault.data.entity.FolderEntity
import com.mydocvault.data.repository.VaultRepository
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
            repository.createFolder(name, folderId)
        }
    }

    fun renameFolder(targetId: Long, name: String) {
        viewModelScope.launch {
            repository.renameFolder(targetId, name)
        }
    }

    fun deleteFolder(targetId: Long) {
        viewModelScope.launch {
            repository.deleteFolderRecursive(targetId)
        }
    }

    fun renameDocument(documentId: Long, name: String) {
        viewModelScope.launch {
            repository.renameDocument(documentId, name)
        }
    }

    fun deleteDocument(documentId: Long) {
        viewModelScope.launch {
            repository.deleteDocument(documentId)
        }
    }

    fun replaceDocument(documentId: Long, uri: android.net.Uri) {
        viewModelScope.launch {
            repository.replaceDocument(documentId, uri)
        }
    }

    fun moveDocument(documentId: Long, newFolderId: Long?) {
        viewModelScope.launch {
            repository.moveDocument(documentId, newFolderId)
        }
    }

    fun importDocument(uri: android.net.Uri, name: String, type: String) {
        viewModelScope.launch {
            repository.importDocument(uri, name, folderId, type)
        }
    }
}
