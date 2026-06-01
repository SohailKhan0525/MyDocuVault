package com.mydocvault.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

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

fun detectFileType(context: Context, uri: Uri, displayName: String): FileType {
    val mimeType = context.contentResolver.getType(uri)
    val fromMime = FileType.fromMimeType(mimeType)
    if (fromMime != FileType.OTHER) return fromMime
    return FileType.fromFileName(displayName)
}

fun shareDocument(context: Context, filePath: String, fileType: FileType) {
    val file = File(filePath)
    if (!file.exists() || !file.canRead()) return
    val contentUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val mimeType = guessMimeType(file.path, fileType)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, "Share via").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}

private fun guessMimeType(path: String, fileType: FileType): String {
    val extension = path.substringAfterLast('.', "").lowercase()
    val extensionMime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    if (!extensionMime.isNullOrBlank()) return extensionMime
    return when (fileType) {
        FileType.IMAGE -> "image/*"
        FileType.PDF -> "application/pdf"
        FileType.DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        FileType.OTHER -> "*/*"
    }
}
