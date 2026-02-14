package com.mydocvault.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mydocvault.ui.components.ConfirmDeleteDialog
import com.mydocvault.ui.components.EmptyState
import com.mydocvault.ui.components.RenameDialog
import com.mydocvault.ui.navigation.NavRoutes
import com.mydocvault.utils.FileType
import com.mydocvault.utils.getDocumentDisplayName
import com.mydocvault.viewmodel.FolderViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FolderDetailScreen(
    navController: NavController,
    viewModel: FolderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val folders by viewModel.subfolders.collectAsState()
    val documents by viewModel.documents.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var renameFolderTarget by remember { mutableStateOf<Long?>(null) }
    var renameDocTarget by remember { mutableStateOf<Long?>(null) }
    var deleteFolderTarget by remember { mutableStateOf<Long?>(null) }
    var deleteDocTarget by remember { mutableStateOf<Long?>(null) }

    val sheetState = rememberModalBottomSheetState()
    val docLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                val name = getDocumentDisplayName(context, uri)
                val type = FileType.fromFileName(name).raw
                viewModel.importDocument(uri, name, type)
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                val name = getDocumentDisplayName(context, uri)
                val type = FileType.fromFileName(name).let { if (it == FileType.OTHER) FileType.IMAGE else it }.raw
                viewModel.importDocument(uri, name, type)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Folder") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (folders.isEmpty() && documents.isEmpty()) {
                EmptyState("Empty folder", "Add subfolders or import a document")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (folders.isNotEmpty()) {
                        items(listOf("Folders")) {
                            Text(text = it, modifier = Modifier.padding(8.dp))
                        }
                        items(folders, key = { it.id }) { folder ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    deleteFolderTarget = folder.id
                                    false
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text("Delete", color = Color.Red)
                                    }
                                }
                            ) {
                                ListItem(
                                    headlineContent = { Text(folder.name) },
                                    modifier = Modifier
                                        .combinedClickable(
                                            onClick = {
                                                navController.navigate("${NavRoutes.Folder}/${folder.id}")
                                            },
                                            onLongClick = { renameFolderTarget = folder.id }
                                        )
                                )
                            }
                        }
                    }

                    if (documents.isNotEmpty()) {
                        items(listOf("Documents")) {
                            Text(text = it, modifier = Modifier.padding(8.dp))
                        }
                        items(documents, key = { it.id }) { doc ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    deleteDocTarget = doc.id
                                    false
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text("Delete", color = Color.Red)
                                    }
                                }
                            ) {
                                ListItem(
                                    headlineContent = { Text(doc.name) },
                                    supportingContent = { Text(doc.fileType) },
                                    modifier = Modifier
                                        .combinedClickable(
                                            onClick = {
                                                navController.navigate("${NavRoutes.Document}/${doc.id}")
                                            },
                                            onLongClick = { renameDocTarget = doc.id }
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ListItem(
                    headlineContent = { Text("New subfolder") },
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            showSheet = false
                            renameFolderTarget = 0L
                        },
                        onLongClick = {}
                    )
                )
                ListItem(
                    headlineContent = { Text("Import from gallery") },
                    supportingContent = { Text("Images only") },
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            showSheet = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onLongClick = {}
                    )
                )
                ListItem(
                    headlineContent = { Text("Import document") },
                    supportingContent = { Text("PDF, DOCX") },
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            showSheet = false
                            docLauncher.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                        },
                        onLongClick = {}
                    )
                )
            }
        }
    }

    if (renameFolderTarget != null) {
        val folderId = renameFolderTarget
        val initial = folders.firstOrNull { it.id == folderId }?.name ?: ""
        val title = if (folderId == 0L) "Create folder" else "Rename folder"
        RenameDialog(
            title = title,
            initial = initial,
            onConfirm = { name ->
                if (folderId == 0L) {
                    viewModel.createFolder(name)
                } else {
                    viewModel.renameFolder(folderId ?: 0L, name)
                }
                renameFolderTarget = null
            },
            onDismiss = { renameFolderTarget = null }
        )
    }

    if (renameDocTarget != null) {
        val docId = renameDocTarget
        val initial = documents.firstOrNull { it.id == docId }?.name ?: ""
        RenameDialog(
            title = "Rename document",
            initial = initial,
            onConfirm = { name ->
                viewModel.renameDocument(docId ?: 0L, name)
                renameDocTarget = null
            },
            onDismiss = { renameDocTarget = null }
        )
    }

    if (deleteFolderTarget != null) {
        ConfirmDeleteDialog(
            title = "Delete folder",
            message = "This will remove all subfolders and documents.",
            onConfirm = {
                viewModel.deleteFolder(deleteFolderTarget ?: 0L)
                deleteFolderTarget = null
            },
            onDismiss = { deleteFolderTarget = null }
        )
    }

    if (deleteDocTarget != null) {
        ConfirmDeleteDialog(
            title = "Delete document",
            message = "This will remove the document file.",
            onConfirm = {
                viewModel.deleteDocument(deleteDocTarget ?: 0L)
                deleteDocTarget = null
            },
            onDismiss = { deleteDocTarget = null }
        )
    }
}
