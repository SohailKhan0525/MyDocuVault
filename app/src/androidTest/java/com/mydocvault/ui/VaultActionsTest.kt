package com.mydocvault.ui

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
        compose.onNodeWithText("New folder").performClick()
        compose.onNodeWithText("Create folder").assertIsDisplayed()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Add files").performClick()
        compose.onNodeWithText("Import any file").assertIsDisplayed()
        compose.onNodeWithText("Import from gallery").assertIsDisplayed()
        compose.onNodeWithText("Images to PDF").assertIsDisplayed()
    }
}
