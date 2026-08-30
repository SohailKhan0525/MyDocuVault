package com.mydocvault.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.mydocvault.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class VaultFileManager(
    private val context: Context,
    private val userPreferences: UserPreferences
) {
    suspend fun getBaseStorageDir(): File = withContext(Dispatchers.IO) {
        val location = userPreferences.storageLocationFlow.first()
        resolveStorageDir(location)
    }

    fun resolveStorageDir(location: String): File {
        val baseDir = when (location) {
            UserPreferences.STORAGE_DOWNLOADS -> {
                val publicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyDocuVault")
                if (publicDir.exists() || publicDir.mkdirs()) {
                    publicDir
                } else {
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        ?: File(context.filesDir, "downloads")
                }
            }
            UserPreferences.STORAGE_DOCUMENTS -> {
                val publicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MyDocuVault")
                if (publicDir.exists() || publicDir.mkdirs()) {
                    publicDir
                } else {
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                        ?: File(context.filesDir, "documents")
                }
            }
            else -> {
                File(context.filesDir, "documents")
            }
        }
        baseDir.mkdirs()
        return baseDir
    }

    suspend fun createFolderDirectory(folderRelativePath: String): File = withContext(Dispatchers.IO) {
        val baseDir = getBaseStorageDir()
        val folderDir = if (folderRelativePath.isBlank()) baseDir else File(baseDir, folderRelativePath)
        folderDir.mkdirs()
        folderDir
    }

    suspend fun importDocument(
        uri: Uri,
        displayName: String,
        folderRelativePath: String? = null
    ): String = withContext(Dispatchers.IO) {
        val baseDir = getBaseStorageDir()
        val targetDir = if (folderRelativePath.isNullOrBlank()) {
            baseDir
        } else {
            File(baseDir, folderRelativePath).apply { mkdirs() }
        }

        val sanitizedName = displayName.trim().ifBlank { "Document_${System.currentTimeMillis()}" }
        var target = File(targetDir, sanitizedName)
        if (target.exists()) {
            val dotIndex = sanitizedName.lastIndexOf('.')
            val baseName = if (dotIndex > 0) sanitizedName.substring(0, dotIndex) else sanitizedName
            val ext = if (dotIndex > 0) sanitizedName.substring(dotIndex) else ""
            target = File(targetDir, "${baseName}_${System.currentTimeMillis()}$ext")
        }
        target.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input ->
            writeStream(input, target)
        } ?: error("Unable to open input stream")
        target.absolutePath
    }

    suspend fun replaceDocument(path: String, uri: Uri) = withContext(Dispatchers.IO) {
        val target = File(path)
        target.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input ->
            writeStream(input, target)
        } ?: error("Unable to open input stream")
    }

    fun deleteDocument(path: String) {
        val target = File(path)
        if (target.exists()) {
            target.delete()
        }
    }

    fun deleteFolder(folderRelativePath: String) {
        val dirs = listOf(
            resolveStorageDir(UserPreferences.STORAGE_DOCUMENTS),
            resolveStorageDir(UserPreferences.STORAGE_DOWNLOADS),
            resolveStorageDir(UserPreferences.STORAGE_INTERNAL)
        )
        dirs.forEach { base ->
            val target = File(base, folderRelativePath)
            if (target.exists()) {
                target.deleteRecursively()
            }
        }
    }

    private fun writeStream(input: InputStream, target: File) {
        FileOutputStream(target).use { output ->
            input.copyTo(output)
        }
    }
}
