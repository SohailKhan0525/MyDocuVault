package com.mydocvault.ui.components

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mydocvault.utils.UpdateNotificationHelper
import com.mydocvault.workers.ReleasePushService
import com.mydocvault.workers.UpdateWorker

@Composable
internal fun ReleaseAlertSetting(firstRun: Boolean) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("release_alerts", Context.MODE_PRIVATE) }
    fun enabled() = NotificationManagerCompat.from(context).areNotificationsEnabled() &&
        context.getSystemService(NotificationManager::class.java)
            .getNotificationChannel(UpdateNotificationHelper.CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE
    var active by remember { mutableStateOf(enabled()) }
    var dismissed by remember { mutableStateOf(prefs.getBoolean("dismissed", false)) }
    fun refresh() {
        active = enabled()
        if (active) {
            ReleasePushService.subscribe(context)
            UpdateWorker.checkNow(context)
        }
    }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refresh() }
    if (firstRun && (active || dismissed)) return
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(if (firstRun) "Hear about new releases" else "Release notifications", style = MaterialTheme.typography.titleMedium)
        Text(if (active) "On · One alert per new version." else "Get a short note when there’s a new version, even with the vault closed.",
            style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = {
                if (Build.VERSION.SDK_INT >= 33 && !NotificationManagerCompat.from(context).areNotificationsEnabled()
                    && !prefs.getBoolean("asked", false)) {
                    prefs.edit().putBoolean("asked", true).apply()
                    request.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    val appEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                    context.startActivity(Intent(if (appEnabled) Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS
                        else Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        .putExtra(Settings.EXTRA_CHANNEL_ID, UpdateNotificationHelper.CHANNEL_ID))
                }
            }) { Text(if (active) "Manage alerts" else "Turn on alerts") }
            if (firstRun) TextButton(onClick = {
                dismissed = true
                prefs.edit().putBoolean("dismissed", true).apply()
            }) { Text("Later") }
        }
    }
}
