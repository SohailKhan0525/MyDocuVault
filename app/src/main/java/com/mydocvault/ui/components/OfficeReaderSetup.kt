package com.mydocvault.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mydocvault.utils.OfficeReaderPackage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun WithOfficeReader(onOpenElsewhere: () -> Unit, reader: @Composable () -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var installed by remember { mutableStateOf(OfficeReaderPackage.isInstalled(context)) }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) installed = OfficeReaderPackage.isInstalled(context)
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    if (installed) { reader(); return }
    val scope = rememberCoroutineScope()
    var downloading by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    val installer = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        installed = OfficeReaderPackage.isInstalled(context)
    }
    fun install() {
        if (downloading) return
        downloading = true
        error = null
        scope.launch {
            try {
                val file = OfficeReaderPackage.download(context) { progress = it }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                installer.launch(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                error = "Couldn’t install the reader. Check your connection and free space, then try again."
            } finally { downloading = false }
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (context.packageManager.canRequestPackageInstalls()) install()
        else error = "Allow installation from MyDocuVault to add its signed reader."
    }
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 420.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Add the Office reader", style = MaterialTheme.typography.headlineMedium)
            Text("Download LibreOffice once for this device (about 80–100 MB). Then open documents, sheets and slides here, even offline.")
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (downloading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Button(enabled = !downloading, onClick = {
                if (context.packageManager.canRequestPackageInstalls()) install()
                else runCatching { permission.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}"))) }.onFailure { error = "Open Android settings to allow app installs." }
            }) { Text(if (downloading) "Downloading · $progress%" else "Install reader") }
            TextButton(onClick = onOpenElsewhere) { Text("Choose another app") }
        }
    }
}
