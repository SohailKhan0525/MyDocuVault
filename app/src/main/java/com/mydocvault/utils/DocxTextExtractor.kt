package com.mydocvault.utils

import java.io.File
import java.util.zip.ZipFile

object DocxTextExtractor {
    fun extractText(file: File): String {
        val zipFile = ZipFile(file)
        val entry = zipFile.getEntry("word/document.xml") ?: return ""
        val xml = zipFile.getInputStream(entry).bufferedReader().use { it.readText() }
        zipFile.close()
        val withNewlines = xml
            .replace("</w:p>", "\n")
            .replace("</w:br>", "\n")
        return withNewlines.replace(Regex("<[^>]+>"), "").trim()
    }
}
