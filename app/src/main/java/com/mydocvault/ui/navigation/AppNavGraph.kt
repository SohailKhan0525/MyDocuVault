package com.mydocvault.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.mydocvault.ui.screens.DocumentViewerScreen
import com.mydocvault.ui.screens.FolderDetailScreen
import com.mydocvault.ui.screens.HomeScreen
import com.mydocvault.ui.screens.OnboardingScreen
import com.mydocvault.ui.screens.PinScreen
import com.mydocvault.ui.screens.SettingsScreen
import com.mydocvault.ui.screens.SplashScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    initialDocumentId: Long? = null
) {
    NavHost(navController = navController, startDestination = NavRoutes.Splash) {
        composable(NavRoutes.Splash) {
            SplashScreen(navController, initialDocumentId = initialDocumentId)
        }
        composable(NavRoutes.Onboarding) {
            OnboardingScreen(navController)
        }
        composable(
            route = "${NavRoutes.Pin}?mode={mode}",
            arguments = listOf(navArgument("mode") { type = NavType.StringType; defaultValue = "unlock" })
        ) {
            PinScreen(navController, initialDocumentId = initialDocumentId)
        }
        composable(NavRoutes.Home) {
            HomeScreen(navController)
        }
        composable(
            route = "${NavRoutes.Folder}/{folderId}",
            arguments = listOf(navArgument("folderId") { type = NavType.LongType })
        ) {
            FolderDetailScreen(navController)
        }
        composable(
            route = "${NavRoutes.Document}/{documentId}",
            arguments = listOf(navArgument("documentId") { type = NavType.LongType })
        ) {
            DocumentViewerScreen(navController)
        }
        composable(NavRoutes.Settings) {
            SettingsScreen(navController)
        }
    }
}
