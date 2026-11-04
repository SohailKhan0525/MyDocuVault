package com.mydocvault.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.CompressorStreamFactory
import java.io.File
import java.util.zip.ZipFile

@Composable
fun ArchiveFileViewer(filePath: String) {
    val entries = produceState<List<String>?>(initialValue = null, filePath) {
        value = withContext(Dispatchers.IO) { listArchiveEntries(File(filePath)) }
    }.value

    if (entries == null) {
        Text("Reading archive…", modifier = Modifier.padding(24.dp))
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Archive contents", style = MaterialTheme.typography.titleMedium)
        if (entries.isEmpty()) Text("This archive is empty.")
        if (entries.size > 1000) Text("Showing the first 1,000 entries.")
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(entries.take(1000)) { entry ->
                ListItem(
                    headlineContent = { Text(entry, maxLines = 2) },
                    leadingContent = {
                        Icon(Icons.Default.InsertDriveFile, contentDescription = null)
                    }
                )
            }
        }
    }
}

private fun listArchiveEntries(file: File): List<String> = runCatching {
    when {
        file.extension.equals("7z", ignoreCase = true) -> {
            SevenZFile(file).use { archive ->
                buildList<String> {
                    var entry = archive.nextEntry
                    while (entry != null && size < 1001) {
                        add(entry.name)
                        entry = archive.nextEntry
                    }
                }
            }
        }
        file.name.endsWith(".tar", ignoreCase = true) ||
            file.name.endsWith(".tar.gz", ignoreCase = true) ||
            file.name.endsWith(".tgz", ignoreCase = true) ||
            file.name.endsWith(".tar.bz2", ignoreCase = true) ||
            file.name.endsWith(".tbz2", ignoreCase = true) ||
            file.name.endsWith(".tar.xz", ignoreCase = true) -> {
            file.inputStream().buffered().use { source ->
                val archiveSource = if (file.name.endsWith(".tar", ignoreCase = true)) {
                    source
                } else {
                    CompressorStreamFactory().createCompressorInputStream(source)
                }
                TarArchiveInputStream(archiveSource).use { archive ->
                    buildList<String> {
                        var entry = archive.nextEntry
                        while (entry != null && size < 1001) {
                            add(entry.name)
                            entry = archive.nextEntry
                        }
                    }
                }
            }
        }
        else -> ZipFile(file).use { zip -> zip.entries().asSequence().take(1001).map { it.name }.toList() }
    }
}.getOrElse { listOf("Unable to read this archive: ${it.message ?: "unsupported format"}") }
