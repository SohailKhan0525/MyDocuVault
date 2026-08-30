package com.mydocvault.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mydocvault.ui.components.ConfirmDeleteDialog
import com.mydocvault.ui.components.EmptyState
import com.mydocvault.ui.components.RenameDialog
import com.mydocvault.ui.navigation.NavRoutes
import com.mydocvault.utils.FileType
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
    var isSearchActive by remember { mutableStateOf(false) }

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
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "MyDocVault",
                                style = MaterialTheme.typography.titleLarge
                            )
                            val totalItems = folders.size + rootDocuments.size
                            Text(
                                text = "$totalItems item${if (totalItems != 1) "s" else ""}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                        "DOCS" to "Word / Docs"
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
                            subtitle = "Type document name, notes, or extension",
                            icon = Icons.Default.Search
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
                                subtitle = "Try a different search keyword or filter",
                                icon = Icons.Default.Search
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (showFolders && searchedFolders.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Matching Folders (${searchedFolders.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
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
                                        modifier = Modifier.padding(top = if (showFolders && searchedFolders.isNotEmpty()) 12.dp else 0.dp, start = 4.dp, bottom = 4.dp)
                                    )
                                }
                                items(searchedDocuments, key = { "d_${it.id}" }) { doc ->
                                    val fileType = FileType.fromRaw(doc.fileType)
                                    val docIcon = when (fileType) {
                                        FileType.IMAGE -> Icons.Default.Image
                                        FileType.PDF   -> Icons.Default.Description
                                        else           -> Icons.Default.FileOpen
                                    }
                                    val docIconColor = when (fileType) {
                                        FileType.IMAGE -> MaterialTheme.colorScheme.secondary
                                        FileType.PDF   -> MaterialTheme.colorScheme.error
                                        else           -> MaterialTheme.colorScheme.tertiary
                                    }
                                    ItemDocCard(
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
                // Regular view
                if (folders.isEmpty() && rootDocuments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        EmptyState(
                            title = "No folders or files yet",
                            subtitle = "Tap + to create a vault folder or import files",
                            icon = Icons.Default.Folder
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (folders.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Folders (${folders.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
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
                                    modifier = Modifier.padding(top = if (folders.isNotEmpty()) 12.dp else 0.dp, start = 4.dp, bottom = 4.dp)
                                )
                            }
                            items(rootDocuments, key = { it.id }) { doc ->
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
                                        FileType.PDF   -> Icons.Default.Description
                                        else           -> Icons.Default.FileOpen
                                    }
                                    val docIconColor = when (fileType) {
                                        FileType.IMAGE -> MaterialTheme.colorScheme.secondary
                                        FileType.PDF   -> MaterialTheme.colorScheme.error
                                        else           -> MaterialTheme.colorScheme.tertiary
                                    }
                                    ItemDocCard(
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
                    text = "Add content",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BottomSheetItem(
                    icon = Icons.Default.CreateNewFolder,
                    title = "New folder",
                    subtitle = "Create an empty vault folder",
                    onClick = {
                        showSheet = false
                        renameFolderTarget = 0L
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
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
                    title = "Import document",
                    subtitle = "PDF, DOCX",
                    onClick = {
                        showSheet = false
                        docLauncher.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderCard(
    name: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemDocCard(
    name: String,
    subtitle: String,
    icon: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconContainerColor,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
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
