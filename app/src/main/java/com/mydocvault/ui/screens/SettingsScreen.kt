package com.mydocvault.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mydocvault.ui.navigation.NavRoutes
import com.mydocvault.viewmodel.SettingsViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()
    val isChecking by viewModel.isChecking.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val error by viewModel.error.collectAsState()
    val isBackingUp by viewModel.isBackingUp.collectAsState()
    val isRestoring by viewModel.isRestoring.collectAsState()
    val backupMessage by viewModel.backupMessage.collectAsState()
    val restartApp by viewModel.restartApp.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Restart the app automatically after a successful restore
    LaunchedEffect(restartApp) {
        if (restartApp) {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            // Kill the current process so the app restarts fresh with the restored data
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    // Only open the update dialog when updateInfo first becomes non-null (new update found)
    LaunchedEffect(updateInfo) {
        if (updateInfo != null) {
            showUpdateDialog = true
        }
    }

    // Launcher to pick a backup ZIP file for restore
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val tmpFile = File(context.cacheDir, "restore_pick.zip")
            val copied = try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tmpFile.outputStream().use { output -> input.copyTo(output) }
                }
                true
            } catch (_: Exception) {
                false
            }
            if (copied) {
                viewModel.restoreBackup(tmpFile)
            } else {
                viewModel.setBackupMessage("Could not read the selected file. Please try again.")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Security card
            SettingsCard {
                SettingsSectionHeader(
                    icon = Icons.Default.Security,
                    title = "Security"
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Biometric toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Biometric unlock",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Use fingerprint or face to unlock",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { viewModel.setBiometricEnabled(it) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Change PIN button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Change PIN",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Update your 4-digit vault PIN",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { navController.navigate("${NavRoutes.Pin}?mode=create") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Change", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Storage Location Card
            val storageLocation by viewModel.storageLocation.collectAsState()
            var showStorageDialog by remember { mutableStateOf(false) }

            SettingsCard {
                SettingsSectionHeader(
                    icon = androidx.compose.material.icons.filled.Folder,
                    title = "Storage Location"
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = androidx.compose.material.icons.filled.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val currentLabel = when (storageLocation) {
                            com.mydocvault.data.preferences.UserPreferences.STORAGE_DOWNLOADS -> "Downloads (/Download/MyDocuVault)"
                            com.mydocvault.data.preferences.UserPreferences.STORAGE_INTERNAL -> "Internal App Storage (Private)"
                            else -> "Documents (/Documents/MyDocuVault)"
                        }
                        Text(
                            text = "Save files to",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = currentLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { showStorageDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Change", style = MaterialTheme.typography.labelLarge)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "New folders and imported files will be organized in this location. Your existing files remain completely safe and accessible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showStorageDialog) {
                AlertDialog(
                    onDismissRequest = { showStorageDialog = false },
                    shape = RoundedCornerShape(24.dp),
                    title = { Text("Choose Storage Location") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val options = listOf(
                                com.mydocvault.data.preferences.UserPreferences.STORAGE_DOCUMENTS to "Documents (Documents/MyDocuVault)",
                                com.mydocvault.data.preferences.UserPreferences.STORAGE_DOWNLOADS to "Downloads (Download/MyDocuVault)",
                                com.mydocvault.data.preferences.UserPreferences.STORAGE_INTERNAL to "Internal App Storage (Private & Sandboxed)"
                            )
                            options.forEach { (key, title) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .androidx.compose.foundation.clickable {
                                            viewModel.setStorageLocation(key)
                                            showStorageDialog = false
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.material3.RadioButton(
                                        selected = storageLocation == key,
                                        onClick = {
                                            viewModel.setStorageLocation(key)
                                            showStorageDialog = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = title, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showStorageDialog = false }) {
                            Text("Done")
                        }
                    }
                )
            }

            // Backup & Restore card
            SettingsCard {
                SettingsSectionHeader(
                    icon = Icons.Default.Backup,
                    title = "Backup & Restore"
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Backup row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            if (isBackingUp) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Backup,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Back up now",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Save to app storage/MyDocuVaultBackup (accessible via file manager)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { viewModel.createBackup() },
                        enabled = !isBackingUp && !isRestoring,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBackingUp) "Saving…" else "Backup",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Restore row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            if (isRestoring) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Restore backup",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Pick a .zip backup file to restore",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { restoreLauncher.launch(arrayOf("application/zip", "*/*")) },
                        enabled = !isBackingUp && !isRestoring,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isRestoring) "Restoring…" else "Restore",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Auto-backup runs every 2 days when battery is not low.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Updates card
            SettingsCard {
                SettingsSectionHeader(
                    icon = Icons.Default.SystemUpdate,
                    title = "Updates"
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "App updates",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Check for the latest version",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { viewModel.checkForUpdate(context, "SohailKhan0525", "MyDocuVault") },
                        enabled = !isChecking,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (isChecking) "Checking…" else "Check",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Footer
            Text(
                text = "Made with ❤️ by Mohd Zaheer Uddin",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }

    if (showUpdateDialog && updateInfo != null) {
        val info = updateInfo!!
        val isDownloading = downloadProgress != null
        AlertDialog(
            onDismissRequest = {
                showUpdateDialog = false
                viewModel.clearUpdateInfo()
            },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Update available — ${info.versionName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = info.notes.ifBlank { "A new version is available." },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (isDownloading) {
                        LinearProgressIndicator(
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
                Button(
                    onClick = {
                        showUpdateDialog = false
                        viewModel.downloadAndInstall(context, info.apkUrl)
                    },
                    enabled = !isDownloading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUpdateDialog = false
                        viewModel.clearUpdateInfo()
                    },
                    enabled = !isDownloading
                ) {
                    Text("Later")
                }
            }
        )
    }

    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Update") },
            text = { Text(error ?: "", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearError() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (backupMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearBackupMessage() },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Backup & Restore") },
            text = { Text(backupMessage ?: "", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearBackupMessage() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
