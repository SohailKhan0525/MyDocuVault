package com.mydocvault.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydocvault.data.entity.DocumentEntity
import com.mydocvault.data.entity.FolderEntity
import com.mydocvault.data.repository.VaultRepository
import com.mydocvault.utils.GlobalErrorBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val repository: VaultRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val documentId: Long = savedStateHandle.get<Long>("documentId") ?: 0L

    private val _document = MutableStateFlow<DocumentEntity?>(null)
    val document: StateFlow<DocumentEntity?> = _document

    val folders: StateFlow<List<FolderEntity>> = repository.allFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            try {
                _document.value = repository.getDocument(documentId)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Load document failed")
            }
        }
    }

    fun moveDocument(documentId: Long, folderId: Long?) {
        viewModelScope.launch {
            try {
                repository.moveDocument(documentId, folderId)
                _document.value = repository.getDocument(documentId)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Move document failed")
            }
        }
    }

    fun replaceDocument(documentId: Long, uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                repository.replaceDocument(documentId, uri)
                _document.value = repository.getDocument(documentId)
            } catch (e: Exception) {
                GlobalErrorBus.emit(e, "Replace document failed")
            }
        }
    }
}
