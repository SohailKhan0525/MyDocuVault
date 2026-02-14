package com.mydocvault.utils

enum class FileType(val raw: String) {
    IMAGE("image"),
    PDF("pdf"),
    DOCX("docx"),
    OTHER("other");

    companion object {
        fun fromRaw(raw: String): FileType {
            return entries.firstOrNull { it.raw == raw } ?: OTHER
        }

        fun fromFileName(name: String): FileType {
            val lower = name.lowercase()
            return when {
                lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") -> IMAGE
                lower.endsWith(".pdf") -> PDF
                lower.endsWith(".docx") -> DOCX
                else -> OTHER
            }
        }
    }
}
