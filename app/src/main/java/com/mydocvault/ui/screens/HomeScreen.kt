package com.mydocvault.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.mydocvault.ui.components.VaultFileRow
import com.mydocvault.ui.theme.vaultWash
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mydocvault.ui.components.ConfirmDeleteDialog
import com.mydocvault.ui.components.EmptyState
import com.mydocvault.ui.components.RenameDialog
import com.mydocvault.ui.navigation.NavRoutes
import com.mydocvault.utils.FileType
import com.mydocvault.utils.ImageToPdfConverter
import com.mydocvault.utils.getDocumentDisplayName
import com.mydocvault.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val folders by viewModel.folders.collectAsState()
    val rootDocuments by viewModel.rootDocuments.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val searchedFolders by viewModel.searchedFolders.collectAsState()
    val searchedDocuments by viewModel.searchedDocuments.collectAsState()

    val updateInfo by viewModel.updateInfo.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val isCreatingPdf by viewModel.isCreatingPdf.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(actionMessage) {
        val message = actionMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearActionMessage()
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.checkForUpdate(context)
    }

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

    val imageToPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            if (uris.isNotEmpty()) viewModel.createPdfFromImages(context, uris)
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Search files, folders, notes…") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.clearSearch()
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close search")
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear query")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "MyDocuVault",
                                style = MaterialTheme.typography.titleMedium
                            )

                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { navController.navigate(NavRoutes.Settings) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (!isSearchActive) {
                Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding().wrapContentWidth(Alignment.CenterHorizontally)
                    .widthIn(max = 840.dp).fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { renameFolderTarget = 0L },
                        modifier = Modifier.weight(1f).heightIn(min = 50.dp)) {
                        Text("New folder")
                    }
                    Button(onClick = { showSheet = true },
                        modifier = Modifier.weight(1f).heightIn(min = 50.dp)) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add files")
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)
            .wrapContentWidth(Alignment.CenterHorizontally).widthIn(max = 840.dp).fillMaxWidth()) {
            if (isSearchActive) {
                // Filter chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "All",
                        "FOLDERS" to "Folders",
                        "IMAGES" to "Images",
                        "PDFS" to "PDFs",
                        "DOCS" to "Word / Docs",
                        "SHEETS" to "Spreadsheets",
                        "SLIDES" to "Presentations"
                    )
                    items(filters) { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { viewModel.onFilterSelected(key) },
                            label = { Text(label) }
                        )
                    }
                }

                if (searchQuery.isBlank()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            title = "Search your vault",
                            subtitle = "Type document name, notes, or extension"
                        )
                    }
                } else {
                    val showFolders = selectedFilter == "ALL" || selectedFilter == "FOLDERS"
                    val showDocs = selectedFilter != "FOLDERS"

                    val hasResults = (showFolders && searchedFolders.isNotEmpty()) || (showDocs && searchedDocuments.isNotEmpty())

                    if (!hasResults) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                title = "No matches found",
                                subtitle = "Try a different search keyword or filter"
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            if (showFolders && searchedFolders.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Matching Folders (${searchedFolders.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                                    )
                                }
                                items(searchedFolders, key = { "f_${it.id}" }) { folder ->
                                    FolderCard(
                                        name = folder.name,
                                        onClick = { navController.navigate("${NavRoutes.Folder}/${folder.id}") },
                                        onLongClick = { renameFolderTarget = folder.id }
                                    )
                                }
                            }

                            if (showDocs && searchedDocuments.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Matching Documents (${searchedDocuments.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = if (showFolders && searchedFolders.isNotEmpty()) 12.dp else 0.dp, start = 8.dp, bottom = 8.dp)
                                    )
                                }
                                items(searchedDocuments, key = { "d_${it.id}" }) { doc ->
                                    val fileType = FileType.fromRaw(doc.fileType)
                                    val docIcon = when (fileType) {
                                        FileType.IMAGE -> Icons.Default.Image
                                        FileType.PDF -> Icons.Default.Description
                                        FileType.SPREADSHEET -> Icons.Default.TableChart
                                        FileType.PRESENTATION -> Icons.Default.Slideshow
                                        else -> Icons.Default.FileOpen
                                    }
                                    val docIconColor = when (fileType) {
                                        FileType.IMAGE -> MaterialTheme.colorScheme.secondary
                                        FileType.PDF -> MaterialTheme.colorScheme.error
                                        FileType.SPREADSHEET -> MaterialTheme.colorScheme.secondary
                                        FileType.PRESENTATION -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.tertiary
                                    }
                                    VaultFileRow(
                                        name = doc.name,
                                        subtitle = doc.fileType.uppercase() + if (!doc.notes.isNullOrBlank()) " • ${doc.notes}" else "",
                                        icon = docIcon,
                                        iconContainerColor = docIconColor.copy(alpha = 0.12f),
                                        iconTint = docIconColor,
                                        onClick = { navController.navigate("${NavRoutes.Document}/${doc.id}") },
                                        onLongClick = { renameDocTarget = doc.id }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    item { VaultOverviewCard(folders.size, rootDocuments.size) }
                    if (folders.isEmpty() && rootDocuments.isEmpty()) {
                        item {
                            EmptyState(
                                title = "Add your first file",
                                subtitle = "Tap Add files to import a document or photos."
                            )
                        }
                    }
                    if (folders.isNotEmpty()) {
                        item {
                            Text(
                                text = "Folders (${folders.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                            )
                        }
                        items(folders, key = { "folder_${it.id}" }) { folder ->
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
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = "Delete",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            ) {
                                FolderCard(
                                    name = folder.name,
                                    onClick = { navController.navigate("${NavRoutes.Folder}/${folder.id}") },
                                    onLongClick = { renameFolderTarget = folder.id }
                                )
                            }
                        }
                    }

                    if (rootDocuments.isNotEmpty()) {
                        item {
                            Text(
                                text = "Documents (${rootDocuments.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = if (folders.isNotEmpty()) 12.dp else 0.dp, start = 8.dp, bottom = 8.dp)
                            )
                        }
                        items(rootDocuments, key = { "document_${it.id}" }) { doc ->
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
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            "Delete",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            ) {
                                val fileType = FileType.fromRaw(doc.fileType)
                                val docIcon = when (fileType) {
                                    FileType.IMAGE -> Icons.Default.Image
                                    FileType.PDF -> Icons.Default.Description
                                    FileType.SPREADSHEET -> Icons.Default.TableChart
                                    FileType.PRESENTATION -> Icons.Default.Slideshow
                                    else -> Icons.Default.FileOpen
                                }
                                val docIconColor = when (fileType) {
                                    FileType.IMAGE -> MaterialTheme.colorScheme.secondary
                                    FileType.PDF -> MaterialTheme.colorScheme.error
                                    FileType.SPREADSHEET -> MaterialTheme.colorScheme.secondary
                                    FileType.PRESENTATION -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.tertiary
                                }
                                VaultFileRow(
                                    name = doc.name,
                                    subtitle = doc.fileType.uppercase(),
                                    icon = docIcon,
                                    iconContainerColor = docIconColor.copy(alpha = 0.12f),
                                    iconTint = docIconColor,
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
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Add files",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BottomSheetItem(
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
                BottomSheetItem(
                    icon = Icons.Default.FileOpen,
                    title = "Import any file",
                    subtitle = "Excel, PowerPoint, PDF, images, archives and more",
                    onClick = {
                        showSheet = false
                        docLauncher.launch(arrayOf("*/*"))
                    }
                )
                BottomSheetItem(
                    icon = Icons.Default.PictureAsPdf,
                    title = if (isCreatingPdf) "Creating PDF…" else "Images to PDF",
                    subtitle = if (isCreatingPdf) "Preparing the pages…" else "Combine up to " + ImageToPdfConverter.MAX_IMAGES + " PNG or JPEG images into one PDF",
                    onClick = {
                        if (!isCreatingPdf) {
                            showSheet = false
                            imageToPdfLauncher.launch(arrayOf("image/*"))
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
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

    if (renameDocTarget != null) {
        val docId = renameDocTarget ?: 0L
        val doc = rootDocuments.firstOrNull { it.id == docId } ?: searchedDocuments.firstOrNull { it.id == docId }
        val initial = doc?.name ?: ""
        RenameDialog(
            title = "Rename document",
            initial = initial,
            onConfirm = { name ->
                viewModel.renameDocument(docId, name)
                renameDocTarget = null
            },
            onDismiss = { renameDocTarget = null }
        )
    }

    if (deleteDocTarget != null) {
        ConfirmDeleteDialog(
            title = "Delete document",
            message = "Are you sure you want to delete this document?",
            onConfirm = {
                viewModel.deleteDocument(deleteDocTarget ?: 0L)
                deleteDocTarget = null
            },
            onDismiss = { deleteDocTarget = null }
        )
    }

    if (updateInfo != null) {
        val info = updateInfo!!
        val isDownloading = downloadProgress != null
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.dismissUpdateDialog() },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Update Available — ${info.versionName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = info.notes.ifBlank { "A new version of MyDocuVault is available." },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (isDownloading) {
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { (downloadProgress ?: 0) / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Downloading… ${downloadProgress}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = { viewModel.downloadAndInstall(context, info.apkUrl) },
                    enabled = !isDownloading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Download & Install")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { viewModel.dismissUpdateDialog() },
                    enabled = !isDownloading
                ) {
                    Text("Later")
                }
            }
        )
    }
}

@Composable
private fun FolderCard(name: String, onClick: () -> Unit, onLongClick: () -> Unit) {
    VaultFileRow(name, null, Icons.Default.Folder, MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.onPrimaryContainer, onClick, onLongClick)
}

@Composable
private fun BottomSheetItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VaultOverviewCard(folderCount: Int, documentCount: Int) {
    Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.fillMaxWidth().vaultWash().padding(22.dp)) {
            Text("Your vault", style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VaultCount(folderCount, if (folderCount == 1) "Folder" else "Folders", Modifier.weight(1f))
                VaultCount(documentCount, if (documentCount == 1) "Loose file" else "Loose files", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun VaultCount(count: Int, label: String, modifier: Modifier) {
    Column(modifier.semantics(mergeDescendants = true) {}) {
        Text(count.toString(), style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
