package com.mydocvault.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "mydocvault_backup"
        private const val NOTIFICATION_ID_SUCCESS = 1001
        private const val NOTIFICATION_ID_FAILURE = 1002
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Backup & Restore",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for scheduled and manual backup operations"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun notifyBackupSuccess(fileName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("Backup complete")
            .setContentText("Saved to Documents/MyDocuVaultBackup/$fileName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID_SUCCESS, notification)
            } catch (_: SecurityException) {
                // POST_NOTIFICATIONS not granted — silently ignore
            }
        }
    }

    fun notifyBackupFailure() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Backup failed")
            .setContentText("Could not create backup. Check storage availability.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID_FAILURE, notification)
            } catch (_: SecurityException) {
                // POST_NOTIFICATIONS not granted — silently ignore
            }
        }
    }
}
