package com.mydocvault.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import java.io.File
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.mydocvault.MainActivity
import com.mydocvault.ui.screens.HomeScreen
import com.mydocvault.ui.theme.MyDocVaultTheme
import org.junit.Rule
import org.junit.Test

class VaultActionsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun newFolderAndFileImportAreReachableFromTheActionBar() {
        compose.activityRule.scenario.onActivity { activity ->
            activity.setContent {
                MyDocVaultTheme(darkTheme = true) { HomeScreen(rememberNavController()) }
            }
        }
        compose.waitForIdle()
        compose.activityRule.scenario.onActivity { activity ->
            capture(activity.window.decorView, File(activity.filesDir, "test-home-dark.png"))
        }
        compose.onNodeWithText("New folder").performClick()
        compose.onNodeWithText("Create folder").assertIsDisplayed()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Add files").performClick()
        compose.onNodeWithText("Import any file").assertIsDisplayed()
        compose.onNodeWithText("Import from gallery").assertIsDisplayed()
        compose.onNodeWithText("Images to PDF").assertIsDisplayed()
    }
    private fun capture(view: View, path: File) {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        path.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
