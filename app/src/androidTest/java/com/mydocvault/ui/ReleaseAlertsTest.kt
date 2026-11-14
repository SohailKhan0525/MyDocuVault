package com.mydocvault.ui

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mydocvault.utils.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReleaseAlertsTest {
    private fun awaitNotificationState(predicate: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + 5_000
        while (!predicate() && SystemClock.elapsedRealtime() < deadline) SystemClock.sleep(50)
        assertTrue("Android did not finish updating the notification", predicate())
    }

    @Test fun releasePostsOneBrandedAlertWithDetailsAndActions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val helper = UpdateNotificationHelper(context)
        helper.createNotificationChannel()
        assertTrue(helper.canNotify())
        val manager = context.getSystemService(NotificationManager::class.java)
        val seen = context.getSharedPreferences("release_alerts", Context.MODE_PRIVATE)
        seen.edit().remove("last").commit()
        try {
            val info = UpdateInfo("v99.0.0", "Find files faster.", "")
            helper.notifyUpdateAvailable(info)
            awaitNotificationState { manager.activeNotifications.any { it.id == 2001 } }
            val notification = manager.activeNotifications.single { it.id == 2001 }.notification
            assertEquals("Find files faster.", notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT))
            assertEquals(listOf("Open update", "What’s new"), notification.actions.map { it.title.toString() })
            manager.cancel(2001)
            awaitNotificationState { manager.activeNotifications.none { it.id == 2001 } }
            assertEquals(info.versionName, seen.getString("last", null))
            helper.notifyUpdateAvailable(info)
            assertTrue(manager.activeNotifications.none { it.id == 2001 })
        } finally {
            manager.cancel(2001)
            seen.edit().remove("last").commit()
        }
    }

    @Test fun installedReaderHasTheVaultSignatureAndAcceptsAnOfficeFile() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertTrue(OfficeReaderPackage.isInstalled(context))
        @Suppress("DEPRECATION")
        val reader = context.packageManager.getApplicationInfo(OfficeReaderPackage.ID, 0)
        assertTrue(OfficeReaderPackage.isExpectedPackage(context, java.io.File(reader.sourceDir)))
        assertFalse(OfficeReaderPackage.isExpectedPackage(context, java.io.File(context.applicationInfo.sourceDir)))
        val file = java.io.File(context.cacheDir, "Grant test.txt")
        file.writeText("Device integration check")
        try {
            assertTrue(openOfficeInLibreOffice(context, file.path, file.name))
        } finally {
            // The remote activity copies the granted file asynchronously.
            file.deleteOnExit()
        }
    }
}
