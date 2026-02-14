package com.mydocvault.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun getDisplayName(context: Context, uri: Uri): String {
    var name = "Document_${System.currentTimeMillis()}"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) {
                name = it.getString(index)
            }
        }
    }
    return name
}
