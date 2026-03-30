package com.mydocvault.utils

import java.io.File
import java.util.zip.ZipFile

object DocxTextExtractor {
    fun extractText(file: File): String {
        ZipFile(file).use { zipFile ->
            val entry = zipFile.getEntry("word/document.xml") ?: return ""
            val xml = zipFile.getInputStream(entry).bufferedReader().use { it.readText() }
            return xml
                .replace("</w:p>", "\n")
                .replace("</w:br>", "\n")
                .replace(Regex("<[^>]+>"), "")
                .trim()
        }
    }
}
