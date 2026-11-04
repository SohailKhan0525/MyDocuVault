package com.mydocvault.utils

import java.io.File
import java.nio.charset.StandardCharsets
import java.util.zip.ZipFile
import org.apache.poi.hslf.usermodel.HSLFSlideShow
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xwpf.usermodel.XWPFDocument

private const val MAX_PREVIEW_BYTES = 2L * 1024L * 1024L

fun readTextPreview(file: File): String {
    require(file.isFile && file.length() <= MAX_PREVIEW_BYTES) { "File is too large to preview." }
    return file.inputStream().bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
}

object OfficeTextExtractor {
    fun extract(file: File, fileType: FileType): String {
        require(file.isFile) { "File does not exist." }
        val extension = file.extension.lowercase()
        when (extension) {
            "doc", "docx" -> return extractWord(file, extension == "docx")
            "xls", "xlsx", "xlsm", "xlsb", "xlt", "xltx" -> return extractWorkbook(file)
            "ppt", "pptx", "pptm", "pps", "ppsx", "pot", "potx" -> return extractPresentation(file, extension == "pptx" || extension == "pptm" || extension == "ppsx" || extension == "potx")
        }
        val entries = when (fileType) {
            FileType.SPREADSHEET -> listOf("xl/sharedStrings.xml", "content.xml")
            FileType.PRESENTATION -> listOf("ppt/presentation.xml", "content.xml")
            FileType.DOCX, FileType.OFFICE -> listOf("word/document.xml", "content.xml")
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

    private fun extractWord(file: File, modern: Boolean): String =
        file.inputStream().use { input ->
            if (modern) {
                XWPFDocument(input).use { document ->
                    buildList {
                        document.paragraphs.mapTo(this) { it.text }
                        document.tables.flatMapTo(this) { table ->
                            table.rows.map { row -> row.tableCells.joinToString("\t") { it.text } }
                        }
                    }.filter { it.isNotBlank() }.joinToString("\n")
                }
            } else {
                HWPFDocument(input).use { document -> document.range.text() }
            }
        }

    private fun extractWorkbook(file: File): String =
        file.inputStream().use { input ->
            WorkbookFactory.create(input).use { workbook ->
                workbook.mapIndexed { sheetIndex, sheet ->
                    buildString {
                        append("Sheet ${sheetIndex + 1}: ${sheet.sheetName}\n")
                        sheet.forEach { row ->
                            append(row.joinToString("\t") { cell -> cell.toString() })
                            append('\n')
                        }
                    }.trim()
                }.filter { it.isNotBlank() }.joinToString("\u0000")
            }
        }

    private fun extractPresentation(file: File, modern: Boolean): String =
        file.inputStream().use { input ->
            if (modern) {
                XMLSlideShow(input).use { deck ->
                    deck.slides.mapIndexed { index, slide ->
                        "Slide ${index + 1}\n" + slide.shapes
                            .filterIsInstance<org.apache.poi.xslf.usermodel.XSLFTextShape>()
                            .joinToString("\n") { it.text }
                    }.joinToString("\u0000")
                }
            } else {
                HSLFSlideShow(input).use { deck ->
                    deck.slides.mapIndexed { index, slide ->
                        "Slide ${index + 1}\n" + slide.shapes
                            .filterIsInstance<org.apache.poi.hslf.usermodel.HSLFTextShape>()
                            .joinToString("\n") { it.text }
                    }.joinToString("\u0000")
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
        FileType.OFFICE -> extension in setOf("odt", "ods", "odp")
        FileType.SPREADSHEET -> extension in setOf("xlsx", "xlsm", "xltx", "xltm", "ods")
        FileType.PRESENTATION -> extension in setOf("pptx", "pptm", "ppsx", "potx", "odp")
        else -> false
    }
}
