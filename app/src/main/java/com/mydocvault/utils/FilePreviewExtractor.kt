package com.mydocvault.utils

import java.io.File
import java.nio.charset.StandardCharsets
import java.util.zip.ZipFile

private const val MAX_PREVIEW_BYTES = 2L * 1024L * 1024L

fun readTextPreview(file: File): String {
    require(file.isFile && file.length() <= MAX_PREVIEW_BYTES) { "File is too large to preview." }
    return file.inputStream().bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
}

object OfficeTextExtractor {
    fun extract(file: File, fileType: FileType): String {
        require(file.isFile) { "File does not exist." }
        val entries = when (fileType) {
            FileType.SPREADSHEET -> listOf("xl/sharedStrings.xml", "content.xml")
            FileType.PRESENTATION -> listOf("ppt/presentation.xml", "content.xml")
            FileType.DOCX -> listOf("word/document.xml")
            else -> emptyList()
        }
        require(entries.isNotEmpty()) { "This Office format has no built-in text preview." }

        ZipFile(file).use { zip ->
            val text = entries.asSequence()
                .mapNotNull { entryName -> zip.getEntry(entryName) }
                .map { entry ->
                    zip.getInputStream(entry).bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                }
                .joinToString("\n")
                .let(::stripXml)
                .trim()
            return if (fileType == FileType.SPREADSHEET) {
                extractSheets(zip, text)
            } else if (fileType == FileType.PRESENTATION) {
                extractSlides(zip, text)
            } else {
                text
            }
        }
    }

    private fun extractSheets(zip: ZipFile, fallback: String): String {
        val sharedStrings = zip.getEntry("xl/sharedStrings.xml")?.let { entry ->
            zip.getInputStream(entry).bufferedReader(StandardCharsets.UTF_8).use { reader ->
                Regex("<(?:[a-zA-Z]+:)?t(?:\\s[^>]*)?>(.*?)</(?:[a-zA-Z]+:)?t>")
                    .findAll(reader.readText())
                    .map { stripXml(it.groupValues[1]) }
                    .toList()
            }
        }.orEmpty()
        val sheets = zip.entries().asSequence()
            .filter { it.name.matches(Regex("xl/worksheets/sheet\\d+\\.xml")) }
            .sortedBy { it.name }
            .mapIndexed { index, entry ->
                val body = zip.getInputStream(entry).bufferedReader(StandardCharsets.UTF_8).use {
                    val xml = it.readText()
                    val resolved = Regex(
                        "<c([^>]*)t=\"s\"([^>]*)>\\s*<v>(\\d+)</v>\\s*</c>"
                    ).replace(xml) { match ->
                        val value = sharedStrings.getOrNull(match.groupValues[3].toIntOrNull() ?: -1).orEmpty()
                        "<c>${value}</c>"
                    }
                    stripXml(resolved).replace(Regex("\\s+"), " ").trim()
                }
                "Sheet ${index + 1}\n${body.ifBlank { "No readable cells found." }}"
            }
            .toList()
        return (sheets.ifEmpty { listOf(fallback) }).joinToString("\u0000")
    }

    private fun extractSlides(zip: ZipFile, fallback: String): String {
        val slides = zip.entries().asSequence()
            .filter { it.name.matches(Regex("ppt/slides/slide\\d+\\.xml")) }
            .sortedBy { it.name }
            .mapIndexed { index, entry ->
                val body = zip.getInputStream(entry).bufferedReader(StandardCharsets.UTF_8)
                    .use { stripXml(it.readText()).trim() }
                "Slide ${index + 1}\n${body.ifBlank { "No readable text found." }}"
            }
            .toList()
        return (slides.ifEmpty { listOf(fallback) }).joinToString("\u0000")
    }

    private fun stripXml(xml: String): String = xml
        .replace(Regex("<w:tab[^>]*/?>"), "\t")
        .replace(Regex("</(?:w:p|a:p|row|table:table-row|tr)>"), "\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("[ \\t]+"), " ")
        .replace(Regex("\\n{3,}"), "\n\n")
}

fun FileType.supportsInAppPreview(fileName: String): Boolean {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return when (this) {
        FileType.IMAGE, FileType.PDF, FileType.DOCX, FileType.TEXT, FileType.CODE -> true
        FileType.SPREADSHEET -> extension in setOf("xlsx", "xlsm", "xltx", "xltm", "ods")
        FileType.PRESENTATION -> extension in setOf("pptx", "pptm", "ppsx", "potx", "odp")
        else -> false
    }
}
