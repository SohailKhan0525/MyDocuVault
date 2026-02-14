package com.mydocvault.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydocvault.data.entity.FolderEntity
import com.mydocvault.data.repository.VaultRepository
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
            repository.createFolder(name, null)
        }
    }

    fun renameFolder(folderId: Long, name: String) {
        viewModelScope.launch {
            repository.renameFolder(folderId, name)
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            repository.deleteFolderRecursive(folderId)
        }
    }

    fun importDocument(uri: android.net.Uri, name: String, type: String) {
        viewModelScope.launch {
            repository.importDocument(uri, name, null, type)
        }
    }
}
