package com.mydocvault

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mydocvault.data.entity.FolderEntity
import com.mydocvault.ui.theme.MyDocVaultTheme
import com.mydocvault.viewmodel.ImportViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImportActivity : ComponentActivity() {

    private val viewModel: ImportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        
        setContent {
            MyDocVaultTheme(darkTheme = isSystemInDarkTheme()) {
                ImportScreen(viewModel, onFinish = { finish() })
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val uris = mutableListOf<Uri>()

        if (intent.action == Intent.ACTION_SEND) {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            if (uri != null) uris.add(uri)
        } else if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
            val uriList = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            if (uriList != null) uris.addAll(uriList)
        }

        if (uris.isEmpty()) {
            Toast.makeText(this, "No files to import", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.setItems(
            uris = uris,
            nameResolver = { uri -> getFileName(uri) },
            mimeResolver = { uri -> getMimeType(uri) }
        )
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        var name = result ?: "Unknown File"
        
        val fileType = com.mydocvault.utils.FileType.fromFileName(name)
        if (fileType == com.mydocvault.utils.FileType.OTHER) {
            val mimeType = getMimeType(uri)
            var ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            
            // Fallbacks for common mime types if MimeTypeMap fails
            if (ext.isNullOrEmpty()) {
                ext = when (mimeType) {
                    "image/jpeg" -> "jpg"
                    "image/png" -> "png"
                    "application/pdf" -> "pdf"
                    "application/msword" -> "doc"
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
                    "text/plain" -> "txt"
                    else -> null
                }
            }
            
            if (!ext.isNullOrEmpty()) {
                name = "$name.$ext"
            }
        }
        return name
    }

    private fun getMimeType(uri: Uri): String {
        return if (uri.scheme == "content") {
            contentResolver.getType(uri) ?: "*/*"
        } else {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: "*/*"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(viewModel: ImportViewModel, onFinish: () -> Unit) {
    val items by viewModel.items.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val importComplete by viewModel.importComplete.collectAsState()

    if (importComplete) {
        LaunchedEffect(Unit) {
            onFinish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Files") }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onFinish) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { viewModel.importFiles() },
                        enabled = !isImporting && items.isNotEmpty() && items.none { it.isUnsupported }
                    ) {
                        Text(if (isImporting) "Importing..." else "Save")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                FolderSelector(
                    folders = folders,
                    selectedFolder = selectedFolder,
                    onFolderSelected = { viewModel.selectFolder(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Files to Import", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            itemsIndexed(items) { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Original: ${item.originalName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = item.baseName,
                            onValueChange = { viewModel.updateItemName(index, it) },
                            label = { Text("Rename File") },
                            isError = item.isUnsupported,
                            supportingText = if (item.isUnsupported) {
                                { Text("Unsupported file type. Cannot import.", color = MaterialTheme.colorScheme.error) }
                            } else null,
                            trailingIcon = {
                                if (item.extension.isNotEmpty()) {
                                    Text(
                                        text = ".${item.extension}",
                                        modifier = Modifier.padding(end = 16.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = item.notes,
                            onValueChange = { viewModel.updateItemNotes(index, it) },
                            label = { Text("Notes (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSelector(
    folders: List<FolderEntity>,
    selectedFolder: FolderEntity?,
    onFolderSelected: (FolderEntity?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedFolder?.name ?: "Root Directory",
            onValueChange = {},
            readOnly = true,
            label = { Text("Destination Folder") },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Root Directory") },
                onClick = {
                    onFolderSelected(null)
                    expanded = false
                }
            )
            folders.forEach { folder ->
                DropdownMenuItem(
                    text = { Text(folder.name) },
                    onClick = {
                        onFolderSelected(folder)
                        expanded = false
                    }
                )
            }
        }
    }
}
