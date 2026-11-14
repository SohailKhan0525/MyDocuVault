package com.mydocvault.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mydocvault.MainActivity
import com.mydocvault.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateNotificationHelper @Inject constructor(@ApplicationContext private val context: Context) {
    companion object { const val CHANNEL_ID = "mydocvault_updates" }
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val seen = context.getSharedPreferences("release_alerts", Context.MODE_PRIVATE)

    fun createNotificationChannel() {
        manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "App updates", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "One notification when a new vault release is ready." })
    }

    fun canNotify(): Boolean = (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context,
        Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
        NotificationManagerCompat.from(context).areNotificationsEnabled() &&
        manager.getNotificationChannel(CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE

    @Synchronized
    fun notifyUpdateAvailable(update: UpdateInfo) {
        val tag = update.versionName
        if (!canNotify() || !VersionComparator.isNewerVersion(tag, seen.getString("last", "0.0.0") ?: "0.0.0")) return
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val open = PendingIntent.getActivity(context, 2001, Intent(context, MainActivity::class.java)
            .setAction("com.mydocvault.OPEN_UPDATE").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP), flags)
        val notes = PendingIntent.getActivity(context, 2002, Intent(Intent.ACTION_VIEW,
            Uri.parse("https://github.com/SohailKhan0525/MyDocuVault/releases/tag/${Uri.encode(tag)}")), flags)
        val detail = update.notes.lineSequence().map { it.trim().trimStart('#', '-', '*', ' ').replace("**", "") }
            .firstOrNull { it.isNotBlank() }?.take(160) ?: "Open your vault when you’re ready to update."
        val icon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_foreground)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_release_alert).setLargeIcon(icon)
            .setColor(0x0074B8).setSubText("MyDocuVault · $tag")
            .setContentTitle("Your vault has something new").setContentText(detail)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detail))
            .setCategory(NotificationCompat.CATEGORY_STATUS).setOnlyAlertOnce(true)
            .setAutoCancel(true).setContentIntent(open)
            .addAction(0, "Open update", open).addAction(0, "What’s new", notes)
            .build()
        try {
            manager.notify(2001, notification)
            seen.edit().putString("last", tag).commit()
        } catch (_: SecurityException) { /* Permission can change between checking and posting. */ }
    }
}
