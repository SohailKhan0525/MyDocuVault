package com.mydocvault.utils

import android.content.Context
import android.media.MediaScannerConnection
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
        try {
            MediaScannerConnection.scanFile(context, arrayOf(folderDir.absolutePath), null, null)
        } catch (_: Exception) {}
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

        var cleanName = displayName.trim().ifBlank { "Document" }
        val dotIndex = cleanName.lastIndexOf('.')
        val baseName = if (dotIndex > 0) cleanName.substring(0, dotIndex) else cleanName
        var ext = if (dotIndex > 0) cleanName.substring(dotIndex) else ""
        
        if (ext.isBlank()) {
            val mime = context.contentResolver.getType(uri)
            val detectedExt = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
            if (!detectedExt.isNullOrBlank()) {
                ext = ".$detectedExt"
                cleanName = "$baseName$ext"
            }
        }

        var target = File(targetDir, cleanName)
        if (target.exists()) {
            var counter = 1
            while (target.exists()) {
                target = File(targetDir, "$baseName ($counter)$ext")
                counter++
            }
        }
        target.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input ->
            writeStream(input, target)
        } ?: error("Unable to open input stream")
        
        try {
            MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), null, null)
        } catch (_: Exception) {}

        target.absolutePath
    }

    suspend fun renameDocument(currentPath: String, newName: String): String = withContext(Dispatchers.IO) {
        val sourceFile = File(currentPath)
        if (!sourceFile.exists()) return@withContext currentPath
        val parentDir = sourceFile.parentFile ?: return@withContext currentPath

        val sourceExt = sourceFile.extension
        val cleanName = newName.trim()
        val targetName = if (!cleanName.contains('.') && sourceExt.isNotBlank()) {
            "$cleanName.$sourceExt"
        } else {
            cleanName
        }

        var targetFile = File(parentDir, targetName)
        if (targetFile.absolutePath == sourceFile.absolutePath) return@withContext currentPath

        if (targetFile.exists()) {
            val dot = targetName.lastIndexOf('.')
            val base = if (dot > 0) targetName.substring(0, dot) else targetName
            val ext = if (dot > 0) targetName.substring(dot) else ""
            var counter = 1
            while (targetFile.exists()) {
                targetFile = File(parentDir, "$base ($counter)$ext")
                counter++
            }
        }

        if (sourceFile.renameTo(targetFile)) {
            try {
                MediaScannerConnection.scanFile(context, arrayOf(sourceFile.absolutePath, targetFile.absolutePath), null, null)
            } catch (_: Exception) {}
            targetFile.absolutePath
        } else {
            try {
                sourceFile.inputStream().use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                        output.flush()
                    }
                }
                sourceFile.delete()
                try {
                    MediaScannerConnection.scanFile(context, arrayOf(sourceFile.absolutePath, targetFile.absolutePath), null, null)
                } catch (_: Exception) {}
                targetFile.absolutePath
            } catch (_: Exception) {
                currentPath
            }
        }
    }

    suspend fun renameFolder(oldRelativePath: String, newRelativePath: String) = withContext(Dispatchers.IO) {
        val dirs = listOf(
            resolveStorageDir(UserPreferences.STORAGE_DOCUMENTS),
            resolveStorageDir(UserPreferences.STORAGE_DOWNLOADS),
            resolveStorageDir(UserPreferences.STORAGE_INTERNAL)
        )
        dirs.forEach { base ->
            val oldFolder = File(base, oldRelativePath)
            val newFolder = File(base, newRelativePath)
            if (oldFolder.exists()) {
                newFolder.parentFile?.mkdirs()
                if (oldFolder.renameTo(newFolder)) {
                    try {
                        MediaScannerConnection.scanFile(context, arrayOf(oldFolder.absolutePath, newFolder.absolutePath), null, null)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    suspend fun replaceDocument(path: String, uri: Uri) = withContext(Dispatchers.IO) {
        val target = File(path)
        target.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input ->
            writeStream(input, target)
        } ?: error("Unable to open input stream")
        try {
            MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), null, null)
        } catch (_: Exception) {}
    }

    fun deleteDocument(path: String) {
        val target = File(path)
        if (target.exists()) {
            target.delete()
            try {
                MediaScannerConnection.scanFile(context, arrayOf(path), null, null)
            } catch (_: Exception) {}
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
                try {
                    MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), null, null)
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun moveDocument(currentPath: String, newFolderRelativePath: String?): String = withContext(Dispatchers.IO) {
        val sourceFile = File(currentPath)
        if (!sourceFile.exists()) return@withContext currentPath
        val baseDir = getBaseStorageDir()
        val targetDir = if (newFolderRelativePath.isNullOrBlank()) baseDir else File(baseDir, newFolderRelativePath)
        targetDir.mkdirs()
        var targetFile = File(targetDir, sourceFile.name)
        if (targetFile.exists() && targetFile.absolutePath != sourceFile.absolutePath) {
            val dotIndex = sourceFile.name.lastIndexOf('.')
            val baseName = if (dotIndex > 0) sourceFile.name.substring(0, dotIndex) else sourceFile.name
            val ext = if (dotIndex > 0) sourceFile.name.substring(dotIndex) else ""
            var counter = 1
            while (targetFile.exists()) {
                targetFile = File(targetDir, "$baseName ($counter)$ext")
                counter++
            }
        }
        if (sourceFile.renameTo(targetFile)) {
            try {
                MediaScannerConnection.scanFile(context, arrayOf(sourceFile.absolutePath, targetFile.absolutePath), null, null)
            } catch (_: Exception) {}
            targetFile.absolutePath
        } else {
            try {
                sourceFile.inputStream().use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                        output.flush()
                    }
                }
                sourceFile.delete()
                try {
                    MediaScannerConnection.scanFile(context, arrayOf(sourceFile.absolutePath, targetFile.absolutePath), null, null)
                } catch (_: Exception) {}
                targetFile.absolutePath
            } catch (_: Exception) {
                currentPath
            }
        }
    }

    private fun writeStream(input: InputStream, target: File) {
        FileOutputStream(target).use { output ->
            input.copyTo(output)
            output.flush()
        }
    }
}
