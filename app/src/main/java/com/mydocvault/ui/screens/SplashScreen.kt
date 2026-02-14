package com.mydocvault.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mydocvault.ui.navigation.NavRoutes
import com.mydocvault.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val pin by viewModel.pinState.collectAsState()
    LaunchedEffect(pin) {
        delay(600)
        if (pin.isNullOrBlank()) {
            navController.navigate("${NavRoutes.Pin}?mode=create") {
                popUpTo(NavRoutes.Splash) { inclusive = true }
            }
        } else {
            navController.navigate("${NavRoutes.Pin}?mode=unlock") {
                popUpTo(NavRoutes.Splash) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Crossfade(targetState = pin.isNullOrBlank(), label = "splash") {
            Text(
                text = if (it) "MyDocVault" else "Unlocking...",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
