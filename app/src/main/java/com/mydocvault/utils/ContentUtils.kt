package com.mydocvault.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
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
            val name = cursor.getString(nameIndex)
            if (!name.isNullOrBlank()) {
                return name
            }
        }
    }
    val fallback = uri.lastPathSegment?.substringAfterLast('/')
    if (!fallback.isNullOrBlank()) {
        return fallback
    }
    val mimeType = resolver.getType(uri)
    val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
    return "Document_${System.currentTimeMillis()}.$ext"
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

    val rawName = if (!displayName.isNullOrBlank()) displayName.trim() else sourceFile.name
    val sourceExt = sourceFile.extension

    // Make sure extension is properly preserved
    val finalName = if (!rawName.contains('.') && sourceExt.isNotBlank()) {
        "$rawName.$sourceExt"
    } else if (!rawName.contains('.')) {
        val fallbackExt = when (fileType) {
            FileType.PDF -> "pdf"
            FileType.IMAGE -> "jpg"
            FileType.DOCX -> "docx"
            FileType.TEXT -> "txt"
            FileType.ARCHIVE -> "zip"
            FileType.VIDEO -> "mp4"
            FileType.AUDIO -> "mp3"
            else -> ""
        }
        if (fallbackExt.isNotBlank()) "$rawName.$fallbackExt" else rawName
    } else {
        rawName
    }

    // Clean staging folder in cache to prevent stale files with old names
    val shareDir = File(context.cacheDir, "shared_documents").apply { mkdirs() }
    val shareFile = File(shareDir, finalName)

    try {
        FileInputStream(sourceFile).use { input ->
            FileOutputStream(shareFile).use { output ->
                input.copyTo(output)
                output.flush()
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
        else -> {
            val ext = finalName.substringAfterLast('.', "").lowercase()
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
        }
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, contentUri)
        putExtra(Intent.EXTRA_TITLE, finalName)
        putExtra(Intent.EXTRA_SUBJECT, finalName)
        clipData = ClipData.newUri(context.contentResolver, finalName, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, "Share via").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}
