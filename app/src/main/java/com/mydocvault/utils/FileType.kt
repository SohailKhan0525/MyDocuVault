package com.mydocvault.utils

enum class FileType(val raw: String) {
    IMAGE("image"), PDF("pdf"), DOCX("docx"), VIDEO("video"), AUDIO("audio"),
    TEXT("text"), ARCHIVE("archive"), CODE("code"), SPREADSHEET("spreadsheet"),
    PRESENTATION("presentation"), OFFICE("office"), EBOOK("ebook"), FONT("font"),
    DATABASE("database"), OTHER("other");

    companion object {
        fun fromRaw(raw: String): FileType =
            entries.firstOrNull { it.raw == raw.lowercase() } ?: OTHER

        fun fromFileName(name: String): FileType {
            val lower = name.substringAfterLast('/', name).lowercase()
            val extension = lower.substringAfterLast('.', "")
            return when (extension) {
                "jpg", "jpeg", "png", "webp", "gif", "bmp", "tif", "tiff",
                "heic", "heif", "avif", "ico", "jfif" -> IMAGE
                "pdf" -> PDF
                "docx" -> DOCX
                "doc", "odt" -> OFFICE
                "mp4", "mkv", "avi", "mov", "webm", "3gp", "m4v", "mpeg", "mpg" -> VIDEO
                "mp3", "wav", "ogg", "flac", "m4a", "aac", "opus", "mid", "midi" -> AUDIO
                "txt", "md", "csv", "tsv", "log", "xml", "yaml", "yml", "toml", "ini",
                "conf", "properties", "rtf" -> TEXT
                "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "zst", "tgz", "jar", "war",
                "apk", "aab", "dmg", "iso" -> ARCHIVE
                "kt", "kts", "java", "gradle", "py", "c", "h", "cpp", "hpp", "cs", "go",
                "rs", "rb", "swift", "ts", "tsx", "js", "jsx", "php", "sh", "sql", "html",
                "htm", "css", "scss", "json", "ipynb", "dart", "lua", "pl", "r", "scala",
                "clj", "ex", "exs", "erl", "hrl", "vue", "svelte", "graphql", "proto" -> CODE
                "xls", "xlsx", "xlsm", "xlsb", "xlt", "xltx", "ods", "numbers" -> SPREADSHEET
                "ppt", "pptx", "pptm", "pps", "ppsx", "pot", "potx", "odp", "key" -> PRESENTATION
                "epub", "mobi", "azw", "azw3", "fb2", "djvu" -> EBOOK
                "ttf", "otf", "woff", "woff2" -> FONT
                "db", "sqlite", "sqlite3", "mdb", "accdb" -> DATABASE
                else -> OTHER
            }
        }
    }
}