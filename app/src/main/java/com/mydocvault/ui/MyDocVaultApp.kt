package com.mydocvault.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mydocvault.ui.navigation.NavRoutes
import androidx.navigation.compose.rememberNavController
import com.mydocvault.ui.navigation.AppNavGraph
import com.mydocvault.ui.theme.MyDocVaultTheme

@Composable
fun MyDocVaultApp(initialDocumentId: Long? = null, openUpdate: Boolean = false, onUpdateOpened: () -> Unit = {}) {
    val navController = rememberNavController()
    val route = navController.currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(route, openUpdate) {
        // Wait for onboarding and unlock before opening update details.
        if (openUpdate && route in setOf(NavRoutes.Home, NavRoutes.Settings,
                "${NavRoutes.Folder}/{folderId}", "${NavRoutes.Document}/{documentId}")) {
            if (route != NavRoutes.Settings) navController.navigate(NavRoutes.Settings) { launchSingleTop = true }
            navController.currentBackStackEntry?.savedStateHandle?.set("check_update", true)
            onUpdateOpened()
        }
    }
    val darkTheme = isSystemInDarkTheme()
    MyDocVaultTheme(darkTheme = darkTheme) {
        AppNavGraph(navController = navController, initialDocumentId = initialDocumentId)
    }
}
