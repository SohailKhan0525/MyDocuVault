package com.mydocvault.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.mydocvault.ui.navigation.AppNavGraph
import com.mydocvault.ui.theme.MyDocVaultTheme

@Composable
fun MyDocVaultApp(initialDocumentId: Long? = null) {
    val navController = rememberNavController()
    val darkTheme = isSystemInDarkTheme()
    MyDocVaultTheme(darkTheme = darkTheme) {
        AppNavGraph(navController = navController, initialDocumentId = initialDocumentId)
    }
}
