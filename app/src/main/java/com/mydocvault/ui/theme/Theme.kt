package com.mydocvault.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = VaultBlue,
    secondary = VaultCyan,
    surface = VaultSand,
    background = VaultSand,
    onPrimary = Color.White,
    onSurface = Color(0xFF1B1B1F)
)

private val DarkColors = darkColorScheme(
    primary = VaultCyan,
    secondary = VaultBlue,
    surface = VaultSurfaceDark,
    background = VaultNight,
    onPrimary = Color(0xFF071522),
    onSurface = Color(0xFFE6E6E9)
)

@Composable
fun MyDocVaultTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = VaultTypography,
        content = content
    )
}
