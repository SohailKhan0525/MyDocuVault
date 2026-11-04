package com.mydocvault.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import androidx.navigation.compose.composable
import com.mydocvault.ui.screens.GuidePage
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
    NavHost(
        navController = navController, startDestination = NavRoutes.Splash,
        enterTransition = {
            fadeIn(tween(220)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(240), initialOffset = { it / 9 })
        },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = {
            fadeIn(tween(280), initialAlpha = 0.80f) +
                scaleIn(tween(280), initialScale = 0.97f)
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(280), targetOffset = { it / 4 }) +
                scaleOut(tween(280), targetScale = 0.94f) + fadeOut(tween(280))
        }
    ) {
        composable(NavRoutes.Splash) {
            SplashScreen(navController)
        }
        navigation(startDestination = GuidePage.Filing.route, route = NavRoutes.Onboarding) {
            GuidePage.entries.forEach { page ->
                composable(page.route) { OnboardingScreen(navController, page) }
            }
        }
        composable(
            route = NavRoutes.PinPattern,
            arguments = listOf(navArgument("mode") { type = NavType.StringType; defaultValue = "unlock" })
        ) { entry ->
            PinScreen(navController, mode = entry.arguments?.getString("mode") ?: "unlock", initialDocumentId = initialDocumentId)
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
