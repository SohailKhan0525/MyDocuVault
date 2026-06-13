package com.mydocvault.utils

enum class FileType(val raw: String) {
    IMAGE("image"),
    PDF("pdf"),
    DOCX("docx"),
    VIDEO("video"),
    AUDIO("audio"),
    TEXT("text"),
    ARCHIVE("archive"),
    CODE("code"),
    SPREADSHEET("spreadsheet"),
    PRESENTATION("presentation"),
    OTHER("other");

    companion object {
        fun fromRaw(raw: String): FileType {
            return entries.firstOrNull { it.raw == raw } ?: OTHER
        }

        fun fromFileName(name: String): FileType {
            val lower = name.lowercase()
            return when {
                lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp") || lower.endsWith(".gif") || lower.endsWith(".bmp") -> IMAGE
                lower.endsWith(".pdf") -> PDF
                lower.endsWith(".docx") || lower.endsWith(".doc") -> DOCX
                lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".avi") || lower.endsWith(".mov") -> VIDEO
                lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".ogg") || lower.endsWith(".flac") -> AUDIO
                lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".csv") || lower.endsWith(".rtf") -> TEXT
                lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z") || lower.endsWith(".tar") || lower.endsWith(".gz") -> ARCHIVE
                lower.endsWith(".kt") || lower.endsWith(".java") || lower.endsWith(".xml") || lower.endsWith(".json") || lower.endsWith(".html") || lower.endsWith(".css") || lower.endsWith(".js") || lower.endsWith(".py") || lower.endsWith(".cpp") || lower.endsWith(".c") || lower.endsWith(".cs") || lower.endsWith(".go") || lower.endsWith(".rs") || lower.endsWith(".rb") || lower.endsWith(".swift") || lower.endsWith(".ts") || lower.endsWith(".php") || lower.endsWith(".sh") || lower.endsWith(".sql") -> CODE
                lower.endsWith(".xls") || lower.endsWith(".xlsx") || lower.endsWith(".ods") -> SPREADSHEET
                lower.endsWith(".ppt") || lower.endsWith(".pptx") || lower.endsWith(".odp") -> PRESENTATION
                else -> OTHER
            }
        }
    }
}
