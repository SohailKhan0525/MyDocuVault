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

    override fun folderById(folderId: Long) = folderDao.getFolderByIdFlow(folderId)

    override suspend fun getFolder(folderId: Long): FolderEntity? = folderDao.getFolderById(folderId)

    override suspend fun getDocument(documentId: Long): DocumentEntity? = documentDao.getDocumentById(documentId)

    override suspend fun createFolder(name: String, parentId: Long?): Long {
        return folderDao.insert(FolderEntity(name = name, parentFolderId = parentId))
    }

    override suspend fun renameFolder(folderId: Long, newName: String) {
        val folder = folderDao.getFolderById(folderId) ?: return
        folderDao.update(folder.copy(name = newName))
    }

    override suspend fun deleteFolderRecursive(folderId: Long) {
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
    }

    override suspend fun importDocument(
        uri: Uri,
        displayName: String,
        folderId: Long?,
        fileType: String
    ): Long {
        val path = fileManager.importDocument(uri, displayName)
        val now = System.currentTimeMillis()
        val doc = DocumentEntity(
            name = displayName,
            folderId = folderId,
            filePath = path,
            fileType = fileType,
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
        documentDao.update(doc.copy(folderId = newFolderId, updatedAt = System.currentTimeMillis()))
    }
}
