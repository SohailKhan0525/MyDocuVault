package com.mydocvault.data.repository

import android.net.Uri
import com.mydocvault.data.entity.DocumentEntity
import com.mydocvault.data.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

interface VaultRepository {
    fun foldersByParent(parentId: Long?): Flow<List<FolderEntity>>
    fun allFolders(): Flow<List<FolderEntity>>
    fun documentsByFolder(folderId: Long?): Flow<List<DocumentEntity>>
    fun allDocuments(): Flow<List<DocumentEntity>>
    fun searchDocuments(query: String): Flow<List<DocumentEntity>>
    fun searchDocumentsInFolder(folderId: Long?, query: String): Flow<List<DocumentEntity>>
    fun searchFolders(query: String): Flow<List<FolderEntity>>
    fun searchSubfolders(parentId: Long?, query: String): Flow<List<FolderEntity>>
    fun folderById(folderId: Long): Flow<FolderEntity?>

    suspend fun getFolder(folderId: Long): FolderEntity?
    suspend fun getDocument(documentId: Long): DocumentEntity?

    suspend fun createFolder(name: String, parentId: Long?): Long
    suspend fun renameFolder(folderId: Long, newName: String)
    suspend fun deleteFolderRecursive(folderId: Long)

    suspend fun importDocument(
        uri: Uri,
        displayName: String,
        folderId: Long?,
        fileType: String,
        notes: String? = null
    ): Long

    suspend fun renameDocument(documentId: Long, newName: String)
    suspend fun deleteDocument(documentId: Long)
    suspend fun replaceDocument(documentId: Long, uri: Uri)
    suspend fun moveDocument(documentId: Long, newFolderId: Long?)
}
