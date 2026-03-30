package com.mydocvault.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class VaultFileManager(
    private val context: Context
) {
    private val docsDir: File = File(context.filesDir, "documents").apply { mkdirs() }

    suspend fun importDocument(uri: Uri, displayName: String): String = withContext(Dispatchers.IO) {
        val safeName = displayName.replace("[^A-Za-z0-9._-]".toRegex(), "_")
        val target = File(docsDir, "${System.currentTimeMillis()}_$safeName")
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

    private fun writeStream(input: InputStream, target: File) {
        FileOutputStream(target).use { output ->
            input.copyTo(output)
        }
    }
}
