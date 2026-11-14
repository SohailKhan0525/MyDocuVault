package com.mydocvault.ui

import android.graphics.Rect
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.mydocvault.MainActivity
import com.mydocvault.ui.screens.HomeScreen
import com.mydocvault.ui.theme.MyDocVaultTheme
import org.junit.Test

class VaultActionsTest {
    private val automation get() = InstrumentationRegistry.getInstrumentation().uiAutomation

    private fun find(node: AccessibilityNodeInfo?, label: String): Rect? {
        if (node == null) return null
        if (node.isVisibleToUser && node.text?.toString() == label) {
            return Rect().also(node::getBoundsInScreen)
        }
        for (index in 0 until node.childCount) {
            find(node.getChild(index), label)?.let { return it }
        }
        return null
    }

    private fun visible(label: String): Rect {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            find(automation.rootInActiveWindow, label)?.let { if (!it.isEmpty) return it }
            SystemClock.sleep(50)
        }
        throw AssertionError("Could not find visible control: $label")
    }

    private fun tap(label: String) {
        val bounds = visible(label)
        val command = "input tap ${bounds.centerX()} ${bounds.centerY()}"
        ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use { it.readBytes() }
    }

    @Test fun newFolderAndFileImportAreReachableFromTheActionBar() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    MyDocVaultTheme(darkTheme = true) { HomeScreen(rememberNavController()) }
                }
            }
            visible("Your vault")
            scenario.onActivity { activity ->
                val view = activity.window.decorView
                val image = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
                view.draw(android.graphics.Canvas(image))
                java.io.File(activity.filesDir, "test-home-dark.png").outputStream().use {
                    image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                }
                image.recycle()
            }
            tap("New folder")
            visible("Create folder")
            tap("Cancel")
            tap("Add files")
            visible("Import any file")
            visible("Import from gallery")
            visible("Images to PDF")
        }
    }
}
