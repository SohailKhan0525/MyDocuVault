package com.mydocvault.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydocvault.data.entity.DocumentEntity
import com.mydocvault.data.entity.FolderEntity
import com.mydocvault.data.repository.VaultRepository
import com.mydocvault.utils.ImageToPdfConverter
import com.mydocvault.utils.UpdateChecker
import com.mydocvault.utils.UpdateInfo
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
import java.io.File
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: VaultRepository,
    private val updateChecker: UpdateChecker
) : ViewModel() {
    val folders: StateFlow<List<FolderEntity>> = repository.foldersByParent(null)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rootDocuments: StateFlow<List<DocumentEntity>> = repository.documentsByFolder(null)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL") // ALL, FOLDERS, IMAGES, PDFS, DOCS
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Int?>(null)
    val downloadProgress: StateFlow<Int?> = _downloadProgress.asStateFlow()

    private val _isCreatingPdf = MutableStateFlow(false)
    val isCreatingPdf: StateFlow<Boolean> = _isCreatingPdf.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun createPdfFromImages(context: Context, imageUris: List<Uri>) {
        if (imageUris.isEmpty()) return
        if (imageUris.size > ImageToPdfConverter.MAX_IMAGES) {
            _actionMessage.value = "Choose up to 100 images per PDF."
            return
        }
        if (_isCreatingPdf.value) return

        viewModelScope.launch {
            _isCreatingPdf.value = true
            _actionMessage.value = null
            var generatedPdf: File? = null
            try {
                generatedPdf = ImageToPdfConverter(context).convert(imageUris)
                val pdfName = "Images_" + System.currentTimeMillis() + ".pdf"
                repository.importDocument(
                    uri = Uri.fromFile(generatedPdf),
                    displayName = pdfName,
                    folderId = null,
                    fileType = "pdf",
                    notes = "Created from " + imageUris.size + " images"
                )
                _actionMessage.value = "PDF created from " + imageUris.size + " images and saved to your vault."
            } catch (exception: Exception) {
                _actionMessage.value = exception.message?.take(180)
                    ?: "Could not create the PDF. Check the selected files and available storage."
            } finally {
                generatedPdf?.delete()
                _isCreatingPdf.value = false
            }
        }
    }

    fun checkForUpdate(context: Context) {
        viewModelScope.launch {
            try {
                val info = updateChecker.checkForUpdate(context, "SohailKhan0525", "MyDocuVault")
                _updateInfo.value = info
            } catch (_: Exception) {}
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
            } catch (_: Exception) {
            } finally {
                _downloadProgress.value = null
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateInfo.value = null
    }

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
            try {
                repository.importDocument(uri, name, null, type)
            } catch (exception: Exception) {
                _actionMessage.value = "Could not import " + name.take(80) + ": " +
                    (exception.message?.take(100) ?: "file access failed")
            }
        }
    }
}
