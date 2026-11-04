package com.mydocvault.ui.screens

import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mydocvault.ui.navigation.NavRoutes
import com.mydocvault.viewmodel.AuthViewModel

@Composable
fun SplashScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        val mode = viewModel.startMode()
        navController.navigate(if (mode == "guide") NavRoutes.Onboarding else "${NavRoutes.Pin}?mode=$mode") {
            popUpTo(NavRoutes.Splash) { inclusive = true }
        }
    }
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("MyDocuVault", style = MaterialTheme.typography.titleLarge)
        }
    }
}
