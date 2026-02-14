package com.mydocvault.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mydocvault.ui.components.PdfViewer
import com.mydocvault.ui.components.ZoomableImage
import com.mydocvault.utils.DocxTextExtractor
import com.mydocvault.utils.FileType
import com.mydocvault.viewmodel.DocumentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewerScreen(
    navController: NavController,
    viewModel: DocumentViewModel = hiltViewModel()
) {
    val document by viewModel.document.collectAsState()
    val folders by viewModel.folders.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var docxText by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState()
    val replaceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            val doc = document
            if (uri != null && doc != null) {
                viewModel.replaceDocument(doc.id, uri)
            }
        }
    )

    LaunchedEffect(document?.id) {
        val doc = document ?: return@LaunchedEffect
        if (FileType.fromRaw(doc.fileType) == FileType.DOCX) {
            docxText = withContext(Dispatchers.IO) {
                DocxTextExtractor.extractText(File(doc.filePath))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(document?.name ?: "Document") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSheet = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val doc = document
            if (doc != null) {
                when (FileType.fromRaw(doc.fileType)) {
                    FileType.IMAGE -> ZoomableImage(path = doc.filePath)
                    FileType.PDF -> PdfViewer(filePath = doc.filePath)
                    FileType.DOCX -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(docxText.ifBlank { "No preview available" })
                        }
                    }
                    else -> {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Unsupported file type")
                        }
                    }
                }
            }
        }
    }

    if (showSheet && document != null) {
        val doc = document!!
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            ListItem(
                headlineContent = { Text("Replace document") },
                supportingContent = { Text("Pick a new file") },
                modifier = Modifier
                    .clickable {
                        showSheet = false
                        replaceLauncher.launch(arrayOf("image/*", "application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    }
                    .padding(horizontal = 8.dp)
            )
            ListItem(
                headlineContent = { Text("Move to root") },
                supportingContent = { Text("Clear folder assignment") },
                modifier = Modifier
                    .clickable {
                        showSheet = false
                        viewModel.moveDocument(doc.id, null)
                    }
                    .padding(horizontal = 8.dp)
            )
            if (folders.isNotEmpty()) {
                Text(
                    text = "Move to folder",
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
                folders.forEach { folder ->
                    ListItem(
                        headlineContent = { Text(folder.name) },
                        modifier = Modifier
                            .clickable {
                                showSheet = false
                                viewModel.moveDocument(doc.id, folder.id)
                            }
                            .padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
