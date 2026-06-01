package com.mydocvault.ui

import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.mydocvault.ui.navigation.AppNavGraph
import com.mydocvault.ui.theme.MyDocVaultTheme
import com.mydocvault.utils.GlobalErrorBus

@Composable
fun MyDocVaultApp() {
    val navController = rememberNavController()
    val darkTheme = isSystemInDarkTheme()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        GlobalErrorBus.errors.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    MyDocVaultTheme(darkTheme = darkTheme) {
        AppNavGraph(navController = navController)
    }
}
