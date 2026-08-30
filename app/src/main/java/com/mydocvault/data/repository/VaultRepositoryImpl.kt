package com.mydocvault.data.repository

import android.net.Uri
import com.mydocvault.data.dao.DocumentDao
import com.mydocvault.data.dao.FolderDao
import com.mydocvault.data.entity.DocumentEntity
import com.mydocvault.data.entity.FolderEntity
import com.mydocvault.utils.VaultFileManager
import javax.inject.Inject

class VaultRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao,
    private val documentDao: DocumentDao,
    private val fileManager: VaultFileManager
) : VaultRepository {
    override fun foldersByParent(parentId: Long?) = folderDao.getFoldersByParent(parentId)

    override fun allFolders() = folderDao.getAllFolders()

    override fun documentsByFolder(folderId: Long?) = documentDao.getDocumentsByFolder(folderId)

    override fun allDocuments() = documentDao.getAllDocuments()

    override fun searchDocuments(query: String) = documentDao.searchDocuments(query)

    override fun searchDocumentsInFolder(folderId: Long?, query: String) = documentDao.searchDocumentsInFolder(folderId, query)

    override fun searchFolders(query: String) = folderDao.searchFolders(query)

    override fun searchSubfolders(parentId: Long?, query: String) = folderDao.searchFoldersByParent(parentId, query)

    override fun folderById(folderId: Long) = folderDao.getFolderByIdFlow(folderId)

    override suspend fun getFolder(folderId: Long): FolderEntity? = folderDao.getFolderById(folderId)

    override suspend fun getDocument(documentId: Long): DocumentEntity? = documentDao.getDocumentById(documentId)

    private suspend fun getFolderRelativePath(folderId: Long?): String {
        if (folderId == null || folderId == 0L) return ""
        val segments = mutableListOf<String>()
        var currentId: Long? = folderId
        while (currentId != null && currentId != 0L) {
            val folder = folderDao.getFolderById(currentId) ?: break
            segments.add(0, folder.name.replace("[^A-Za-z0-9._ -]".toRegex(), "_"))
            currentId = folder.parentFolderId
        }
        return segments.joinToString("/")
    }

    override suspend fun createFolder(name: String, parentId: Long?): Long {
        val sanitizedName = name.replace("[^A-Za-z0-9._ -]".toRegex(), "_")
        val id = folderDao.insert(FolderEntity(name = name, parentFolderId = parentId))
        val parentPath = getFolderRelativePath(parentId)
        val fullPath = if (parentPath.isEmpty()) sanitizedName else "$parentPath/$sanitizedName"
        try {
            fileManager.createFolderDirectory(fullPath)
        } catch (_: Exception) {
            // best-effort disk creation
        }
        return id
    }

    override suspend fun renameFolder(folderId: Long, newName: String) {
        val folder = folderDao.getFolderById(folderId) ?: return
        folderDao.update(folder.copy(name = newName))
        val parentPath = getFolderRelativePath(folder.parentFolderId)
        val sanitizedName = newName.replace("[^A-Za-z0-9._ -]".toRegex(), "_")
        val newPath = if (parentPath.isEmpty()) sanitizedName else "$parentPath/$sanitizedName"
        try {
            fileManager.createFolderDirectory(newPath)
        } catch (_: Exception) {}
    }

    override suspend fun deleteFolderRecursive(folderId: Long) {
        val folderPath = getFolderRelativePath(folderId)
        val children = folderDao.getFoldersByParentOnce(folderId)
        children.forEach { child ->
            deleteFolderRecursive(child.id)
        }
        val docs = documentDao.getDocumentsByFolderOnce(folderId)
        docs.forEach { doc ->
            fileManager.deleteDocument(doc.filePath)
            documentDao.delete(doc)
        }
        folderDao.deleteById(folderId)
        if (folderPath.isNotEmpty()) {
            try {
                fileManager.deleteFolder(folderPath)
            } catch (_: Exception) {}
        }
    }

    override suspend fun importDocument(
        uri: Uri,
        displayName: String,
        folderId: Long?,
        fileType: String,
        notes: String?
    ): Long {
        val folderPath = getFolderRelativePath(folderId)
        val path = fileManager.importDocument(uri, displayName, folderPath.ifBlank { null })
        val now = System.currentTimeMillis()
        val doc = DocumentEntity(
            name = displayName,
            folderId = folderId,
            filePath = path,
            fileType = fileType,
            notes = notes,
            createdAt = now,
            updatedAt = now
        )
        return documentDao.insert(doc)
    }

    override suspend fun renameDocument(documentId: Long, newName: String) {
        val doc = documentDao.getDocumentById(documentId) ?: return
        documentDao.update(doc.copy(name = newName, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteDocument(documentId: Long) {
        val doc = documentDao.getDocumentById(documentId) ?: return
        fileManager.deleteDocument(doc.filePath)
        documentDao.delete(doc)
    }

    override suspend fun replaceDocument(documentId: Long, uri: Uri) {
        val doc = documentDao.getDocumentById(documentId) ?: return
        fileManager.replaceDocument(doc.filePath, uri)
        documentDao.update(doc.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun moveDocument(documentId: Long, newFolderId: Long?) {
        val doc = documentDao.getDocumentById(documentId) ?: return
        val newFolderPath = getFolderRelativePath(newFolderId)
        val newFilePath = fileManager.moveDocument(doc.filePath, newFolderPath.ifBlank { null })
        documentDao.update(doc.copy(folderId = newFolderId, filePath = newFilePath, updatedAt = System.currentTimeMillis()))
    }
}
