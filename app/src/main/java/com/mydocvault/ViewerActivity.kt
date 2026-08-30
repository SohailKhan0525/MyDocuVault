package com.mydocvault

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.mydocvault.data.entity.FolderEntity
import com.mydocvault.ui.components.PdfViewer
import com.mydocvault.ui.components.RenameDialog
import com.mydocvault.ui.components.ZoomableImage
import com.mydocvault.ui.theme.MyDocVaultTheme
import com.mydocvault.utils.*
import com.mydocvault.viewmodel.ViewerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class ViewerActivity : ComponentActivity() {

    private val viewModel: ViewerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri: Uri? = if (intent.action == Intent.ACTION_VIEW) {
            intent.data
        } else if (intent.action == Intent.ACTION_SEND) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        } else {
            intent.data
        }

        if (uri == null) {
            Toast.makeText(this, "No file to view", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val displayName = getDocumentDisplayName(this, uri)

        setContent {
            MyDocVaultTheme(darkTheme = isSystemInDarkTheme()) {
                ExternalViewerScreen(
                    uri = uri,
                    displayName = displayName,
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExternalViewerScreen(
    uri: Uri,
    displayName: String,
    viewModel: ViewerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val folders by viewModel.folders.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var tempFile by remember { mutableStateOf<File?>(null) }
    var isLoadingTemp by remember { mutableStateOf(true) }
    var docxText by remember { mutableStateOf("") }
    var docxError by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var selectedFolder by remember { mutableStateOf<FolderEntity?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var saveName by remember { mutableStateOf(displayName) }

    val fileType = FileType.fromFileName(displayName)

    LaunchedEffect(uri) {
        isLoadingTemp = true
        withContext(Dispatchers.IO) {
            try {
                val tempDir = File(context.cacheDir, "temp_preview").apply { mkdirs() }
                val targetName = displayName.ifBlank { "preview_${System.currentTimeMillis()}" }
                val file = File(tempDir, targetName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                        output.flush()
                    }
                }
                tempFile = file

                if (fileType == FileType.DOCX) {
                    try {
                        docxText = DocxTextExtractor.extractText(file)
                        docxError = false
                    } catch (_: Exception) {
                        docxError = true
                    }
                }
            } catch (_: Exception) {}
        }
        isLoadingTemp = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save to Vault")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Save to Vault") },
                            leadingIcon = { Icon(Icons.Default.Save, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showSaveDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                val localFile = tempFile
                                if (localFile != null && localFile.exists()) {
                                    shareDocument(context, localFile.absolutePath, displayName, fileType)
                                } else {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "*/*"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Open in External App") },
                            leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                try {
                                    val localFile = tempFile
                                    val targetUri = if (localFile != null && localFile.exists()) {
                                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", localFile)
                                    } else {
                                        uri
                                    }
                                    val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(targetUri, "*/*")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(openIntent, "Open with"))
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Could not open externally", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoadingTemp) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val file = tempFile
                when (fileType) {
                    FileType.IMAGE -> {
                        ZoomableImage(path = file?.absolutePath ?: "")
                    }
                    FileType.PDF -> {
                        if (file != null && file.exists()) {
                            PdfViewer(filePath = file.absolutePath)
                        } else {
                            Text(
                                text = "Unable to load PDF",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    FileType.DOCX -> {
                        if (docxError) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                                    .align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Unable to extract text from this Word document.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = docxText.ifEmpty { "This Word document is empty." },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                                .align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Button(
                                onClick = {
                                    try {
                                        val localFile = tempFile
                                        val targetUri = if (localFile != null && localFile.exists()) {
                                            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", localFile)
                                        } else {
                                            uri
                                        }
                                        val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(targetUri, "*/*")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(openIntent, "Open with"))
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Could not open externally", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("Open in External App")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        var folderMenuExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSaving) showSaveDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Save to Vault") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = saveName,
                        onValueChange = { saveName = it },
                        label = { Text("File Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = folderMenuExpanded,
                        onExpandedChange = { folderMenuExpanded = !folderMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedFolder?.name ?: "Root Directory",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Vault Folder") },
                            trailingIcon = {
                                Icon(
                                    imageVector = if (folderMenuExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = folderMenuExpanded,
                            onDismissRequest = { folderMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Root Directory") },
                                onClick = {
                                    selectedFolder = null
                                    folderMenuExpanded = false
                                }
                            )
                            folders.forEach { folder ->
                                DropdownMenuItem(
                                    text = { Text(folder.name) },
                                    onClick = {
                                        selectedFolder = folder
                                        folderMenuExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("+ Create New Folder…", color = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    folderMenuExpanded = false
                                    showCreateFolderDialog = true
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    if (isSaving) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = saveName.trim().ifBlank { displayName }
                        viewModel.saveToVault(
                            uri = uri,
                            displayName = finalName,
                            folderId = selectedFolder?.id,
                            notes = notes.ifBlank { null },
                            onSuccess = {
                                Toast.makeText(context, "Saved to Vault!", Toast.LENGTH_SHORT).show()
                                showSaveDialog = false
                            }
                        )
                    },
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isSaving) "Saving…" else "Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveDialog = false },
                    enabled = !isSaving
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCreateFolderDialog) {
        RenameDialog(
            title = "Create Folder",
            initial = "",
            onConfirm = { name ->
                if (name.isNotBlank()) {
                    viewModel.createFolder(name) { newFolder ->
                        selectedFolder = newFolder
                    }
                }
                showCreateFolderDialog = false
            },
            onDismiss = { showCreateFolderDialog = false }
        )
    }
}
