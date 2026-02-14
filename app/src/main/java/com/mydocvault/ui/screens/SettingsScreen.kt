package com.mydocvault.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mydocvault.ui.navigation.NavRoutes
import com.mydocvault.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()
    val isChecking by viewModel.isChecking.collectAsState()
    val error by viewModel.error.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (updateInfo != null && !showUpdateDialog) {
        showUpdateDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Security", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    RowSetting(
                        title = "Enable biometric",
                        trailing = {
                            Switch(checked = biometricEnabled, onCheckedChange = { viewModel.setBiometricEnabled(it) })
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { navController.navigate("${NavRoutes.Pin}?mode=create") }) {
                        Text("Change PIN")
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Updates", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.checkForUpdate(context, "SohailKhan0525", "MyDocuVault") }, enabled = !isChecking) {
                        Text(if (isChecking) "Checking..." else "Check for updates")
                    }
                }
            }
        }
    }

    if (showUpdateDialog && updateInfo != null) {
        val info = updateInfo!!
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("Update available ${info.versionName}") },
            text = { Text(info.notes.ifBlank { "A new version is available." }) },
            confirmButton = {
                Button(onClick = {
                    showUpdateDialog = false
                    viewModel.downloadAndInstall(context, info.apkUrl)
                }) {
                    Text("Download")
                }
            },
            dismissButton = {
                Button(onClick = { showUpdateDialog = false }) { Text("Later") }
            }
        )
    }

    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Update") },
            text = { Text(error ?: "") },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun RowSetting(title: String, trailing: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f))
        trailing()
    }
}
