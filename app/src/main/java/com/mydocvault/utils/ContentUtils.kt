package com.mydocvault.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

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
