package com.mydocvault.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydocvault.data.entity.FolderEntity
import com.mydocvault.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImportItem(
    val uri: Uri,
    val originalName: String,
    val mimeType: String,
    val baseName: String,
    val extension: String,
    val notes: String = ""
) {
    val newFullName get() = if (extension.isEmpty()) baseName else "$baseName.$extension"
}

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val repository: VaultRepository
) : ViewModel() {

    private val _folders = MutableStateFlow<List<FolderEntity>>(emptyList())
    val folders: StateFlow<List<FolderEntity>> = _folders.asStateFlow()

    private val _selectedFolder = MutableStateFlow<FolderEntity?>(null)
    val selectedFolder: StateFlow<FolderEntity?> = _selectedFolder.asStateFlow()

    private val _items = MutableStateFlow<List<ImportItem>>(emptyList())
    val items: StateFlow<List<ImportItem>> = _items.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importComplete = MutableStateFlow(false)
    val importComplete: StateFlow<Boolean> = _importComplete.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allFolders().collect { list ->
                _folders.value = list
            }
        }
    }

    fun setItems(uris: List<Uri>, nameResolver: (Uri) -> String, mimeResolver: (Uri) -> String) {
        _items.value = uris.map { uri ->
            val originalName = nameResolver(uri)
            val dotIndex = originalName.lastIndexOf('.')
            val baseName = if (dotIndex >= 0) originalName.substring(0, dotIndex) else originalName
            val extension = if (dotIndex >= 0) originalName.substring(dotIndex + 1) else ""
            
            ImportItem(
                uri = uri,
                originalName = originalName,
                mimeType = mimeResolver(uri),
                baseName = baseName,
                extension = extension
            )
        }
    }

    fun updateItemName(index: Int, newBaseName: String) {
        val current = _items.value.toMutableList()
        current[index] = current[index].copy(baseName = newBaseName)
        _items.value = current
    }

    fun updateItemNotes(index: Int, newNotes: String) {
        val current = _items.value.toMutableList()
        current[index] = current[index].copy(notes = newNotes)
        _items.value = current
    }

    fun selectFolder(folder: FolderEntity?) {
        _selectedFolder.value = folder
    }

    fun importFiles() {
        if (_items.value.isEmpty()) return
        
        viewModelScope.launch {
            _isImporting.value = true
            val folderId = _selectedFolder.value?.id
            _items.value.forEach { item ->
                val finalName = item.newFullName.ifBlank { item.originalName }
                val fileType = com.mydocvault.utils.FileType.fromFileName(finalName).raw
                repository.importDocument(
                    uri = item.uri,
                    displayName = finalName,
                    folderId = folderId,
                    fileType = fileType,
                    notes = item.notes.ifBlank { null }
                )
            }
            _isImporting.value = false
            _importComplete.value = true
        }
    }
}
