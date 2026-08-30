package com.mydocvault.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.mydocvault.data.entity.DocumentEntity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

fun getDocumentDisplayName(context: Context, uri: Uri): String {
    val resolver = context.contentResolver
    resolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            return cursor.getString(nameIndex)
        }
    }
    return uri.lastPathSegment?.substringAfterLast('/') ?: "Imported_${System.currentTimeMillis()}"
}

fun shareDocument(context: Context, document: DocumentEntity) {
    shareDocument(context, document.filePath, document.name, FileType.fromRaw(document.fileType))
}

fun shareDocument(
    context: Context,
    filePath: String,
    displayName: String? = null,
    fileType: FileType = FileType.OTHER
) {
    val sourceFile = File(filePath)
    if (!sourceFile.exists() || !sourceFile.canRead()) return

    val targetName = if (!displayName.isNullOrBlank()) displayName else sourceFile.name

    // Copy to app cache shared_documents with the exact target file name so receiving apps preserve it
    val shareDir = File(context.cacheDir, "shared_documents").apply { mkdirs() }
    val shareFile = File(shareDir, targetName)

    try {
        FileInputStream(sourceFile).use { input ->
            FileOutputStream(shareFile).use { output ->
                input.copyTo(output)
            }
        }
    } catch (_: Exception) {
        // If caching fails, fall back to source file
    }

    val fileToShare = if (shareFile.exists()) shareFile else sourceFile
    val contentUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        fileToShare
    )

    val mimeType = when (fileType) {
        FileType.IMAGE -> "image/*"
        FileType.PDF -> "application/pdf"
        FileType.DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        FileType.VIDEO -> "video/*"
        FileType.AUDIO -> "audio/*"
        FileType.TEXT -> "text/*"
        FileType.ARCHIVE -> "application/zip"
        else -> "*/*"
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, contentUri)
        putExtra(Intent.EXTRA_TITLE, targetName)
        putExtra(Intent.EXTRA_SUBJECT, targetName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, "Share via").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}
