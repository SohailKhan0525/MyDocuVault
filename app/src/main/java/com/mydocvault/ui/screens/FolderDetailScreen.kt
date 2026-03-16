package com.mydocvault.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20),
        onResult = { uris ->
            uris.forEach { uri ->
                val name = getDocumentDisplayName(context, uri)
                val type = FileType.fromFileName(name).let { if (it == FileType.OTHER) FileType.IMAGE else it }.raw
                viewModel.importDocument(uri, name, type)
            }
        }
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Folder",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSheet = true },
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                text = { Text("Add") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (folders.isEmpty() && documents.isEmpty()) {
                EmptyState("Empty folder", "Add subfolders or import a document")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (folders.isNotEmpty()) {
                        item {
                            SectionHeader(text = "Folders", count = folders.size)
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
                                    SwipeDeleteBackground()
                                }
                            ) {
                                FolderItemCard(
                                    name = folder.name,
                                    onClick = { navController.navigate("${NavRoutes.Folder}/${folder.id}") },
                                    onLongClick = { renameFolderTarget = folder.id }
                                )
                            }
                        }
                    }

                    if (documents.isNotEmpty()) {
                        item {
                            SectionHeader(
                                text = "Documents",
                                count = documents.size,
                                topPadding = if (folders.isNotEmpty()) 12.dp else 0.dp
                            )
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
                                    SwipeDeleteBackground()
                                }
                            ) {
                                DocumentItemCard(
                                    name = doc.name,
                                    fileType = doc.fileType,
                                    onClick = { navController.navigate("${NavRoutes.Document}/${doc.id}") },
                                    onLongClick = { renameDocTarget = doc.id }
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
            sheetState = sheetState,
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Add to Folder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.size(4.dp))
                SheetOption(
                    icon = Icons.Default.CreateNewFolder,
                    title = "New subfolder",
                    subtitle = "Organise within this folder",
                    onClick = {
                        showSheet = false
                        renameFolderTarget = 0L
                    }
                )
                SheetOption(
                    icon = Icons.Default.Image,
                    title = "Import from gallery",
                    subtitle = "Images only",
                    onClick = {
                        showSheet = false
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
                SheetOption(
                    icon = Icons.Default.UploadFile,
                    title = "Import document",
                    subtitle = "PDF, DOCX",
                    onClick = {
                        showSheet = false
                        docLauncher.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    }
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

@Composable
private fun SectionHeader(
    text: String,
    count: Int,
    topPadding: Dp = 0.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = 4.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            shape = MaterialTheme.shapes.extraSmall,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun SwipeDeleteBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Delete",
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderItemCard(
    name: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DocumentItemCard(
    name: String,
    fileType: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val (icon, containerColor, iconColor) = when (FileType.fromRaw(fileType)) {
        FileType.IMAGE -> Triple(
            Icons.Default.Image,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.tertiary
        )
        FileType.PDF -> Triple(
            Icons.Default.PictureAsPdf,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error
        )
        FileType.DOCX -> Triple(
            Icons.Default.Description,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary
        )
        else -> Triple(
            Icons.Default.Description,
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = containerColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = fileType.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SheetOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        },
        supportingContent = {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        leadingContent = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(onClick = onClick, onLongClick = {})
    )
}
