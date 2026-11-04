package com.mydocvault.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mydocvault.utils.FileType
import com.mydocvault.utils.openDocumentExternally
import com.mydocvault.utils.openOfficeInLibreOffice
import java.io.File

@Composable
fun FileReader(file: File, name: String, storedType: FileType) {
    val context = LocalContext.current
    val detected = FileType.fromFileName(name)
    val type = if (detected == FileType.OTHER) storedType else detected
    val ext = name.substringAfterLast('.', "").lowercase()
    val openElsewhere = {
        if (!openDocumentExternally(context, file.path, name)) {
            Toast.makeText(context, "Could not open this file.", Toast.LENGTH_SHORT).show()
        }
    }
    when {
        !file.isFile -> ReaderMessage("This file is no longer available.")
        type in setOf(FileType.DOCX, FileType.OFFICE, FileType.SPREADSHEET, FileType.PRESENTATION) ||
            ext == "txt" -> {
            var launched by rememberSaveable(file.path) { mutableStateOf(false) }
            var failed by rememberSaveable(file.path) { mutableStateOf(false) }
            val read = { failed = !openOfficeInLibreOffice(context, file.path, name) }
            LaunchedEffect(file.path) {
                if (!launched) {
                    launched = true
                    read()
                }
            }
            if (!launched) ReaderLoading()
            else if (failed) ReaderMessage("The office reader could not open this file.", "Choose another app", openElsewhere)
            else ReaderMessage(name, "Read document", read)
        }
        ext == "epub" -> EpubReader(file, openElsewhere)
        ext in setOf("ttf", "otf") -> FontSpecimen(file, openElsewhere)
        ext in setOf("sqlite", "sqlite3", "db") -> DatabaseIndex(file, openElsewhere)
        type == FileType.IMAGE -> ZoomableImage(file.path)
        type == FileType.PDF -> PdfViewer(file.path)
        type == FileType.VIDEO || type == FileType.AUDIO -> MediaFileViewer(file.path, openElsewhere)
        type == FileType.ARCHIVE && (ext in setOf("zip", "jar", "war", "apk", "aab", "7z", "tar", "tgz", "tbz2") ||
            name.lowercase().endsWith(".tar.gz") || name.lowercase().endsWith(".tar.bz2") || name.lowercase().endsWith(".tar.xz")) -> ArchiveFileViewer(file.path)
        type == FileType.TEXT || type == FileType.CODE -> SourceReader(file, ext in setOf("md", "markdown"), openElsewhere)
        else -> ReaderMessage("This format needs another reader.", "Choose an app", openElsewhere)
    }
}

@Composable
internal fun ReaderLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
internal fun ReaderMessage(message: String, label: String? = null, onClick: () -> Unit = {}) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (label != null) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onClick) { Text(label) }
        }
    }
}
