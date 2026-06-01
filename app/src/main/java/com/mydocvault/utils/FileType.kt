package com.mydocvault.utils

import android.webkit.MimeTypeMap

enum class FileType(val raw: String) {
    IMAGE("image"),
    PDF("pdf"),
    DOCX("docx"),
    OTHER("other");

    companion object {
        private val imageExtensions = setOf(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "tif", "tiff", "svg", "avif"
        )
        private val docxExtensions = setOf("docx", "docm", "dotx", "dotm")

        fun fromRaw(raw: String): FileType {
            return entries.firstOrNull { it.raw == raw } ?: OTHER
        }

        fun fromFileName(name: String): FileType {
            val lower = name.lowercase()
            val extension = lower.substringAfterLast('.', "")
            val mimeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            return when {
                extension in imageExtensions -> IMAGE
                lower.endsWith(".pdf") -> PDF
                extension in docxExtensions -> DOCX
                mimeFromExt?.startsWith("image/") == true -> IMAGE
                else -> OTHER
            }
        }

        fun fromMimeType(mimeType: String?): FileType {
            val normalized = mimeType?.lowercase().orEmpty()
            return when {
                normalized.startsWith("image/") -> IMAGE
                normalized == "application/pdf" -> PDF
                normalized.contains("wordprocessingml") || normalized == "application/msword" -> DOCX
                else -> OTHER
            }
        }
    }
}
