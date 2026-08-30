package com.mydocvault.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydocvault.data.entity.DocumentEntity
import com.mydocvault.data.entity.FolderEntity
import com.mydocvault.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: VaultRepository
) : ViewModel() {
    val folders: StateFlow<List<FolderEntity>> = repository.foldersByParent(null)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rootDocuments: StateFlow<List<DocumentEntity>> = repository.documentsByFolder(null)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL") // ALL, FOLDERS, IMAGES, PDFS, DOCS
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    val searchedFolders: StateFlow<List<FolderEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.searchFolders(query.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchedDocuments: StateFlow<List<DocumentEntity>> = combine(_searchQuery, _selectedFilter) { query, filter ->
        query to filter
    }.flatMapLatest { (query, filter) ->
        if (query.isBlank()) {
            flowOf(emptyList())
        } else {
            repository.searchDocuments(query.trim()).map { docs ->
                when (filter) {
                    "IMAGES" -> docs.filter { it.fileType == "image" }
                    "PDFS" -> docs.filter { it.fileType == "pdf" }
                    "DOCS" -> docs.filter { it.fileType == "docx" || it.fileType == "doc" || it.fileType == "text" }
                    else -> docs
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _selectedFilter.value = "ALL"
    }

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

    fun importDocument(uri: android.net.Uri, name: String, type: String) {
        viewModelScope.launch {
            repository.importDocument(uri, name, null, type)
        }
    }
}
