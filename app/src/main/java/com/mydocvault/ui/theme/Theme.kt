package com.mydocvault.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary            = VaultBlue,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001B4F),
    secondary          = VaultIndigo,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFE0E0FF),
    onSecondaryContainer = Color(0xFF0D0066),
    surface            = VaultSandCard,
    onSurface          = Color(0xFF1A1B2E),
    surfaceVariant     = Color(0xFFEEF0FF),
    onSurfaceVariant   = Color(0xFF44466A),
    background         = VaultSand,
    onBackground       = Color(0xFF1A1B2E),
    error              = ErrorRed,
    onError            = OnErrorLight,
    outline            = Color(0xFFB0B4D4),
    outlineVariant     = Color(0xFFDDE0F5)
)

private val DarkColors = darkColorScheme(
    primary            = VaultBlueDark,
    onPrimary          = Color(0xFF002980),
    primaryContainer   = Color(0xFF003DA6),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary          = Color(0xFFA6A8FF),
    onSecondary        = Color(0xFF1A1B6E),
    secondaryContainer = Color(0xFF2E2F9E),
    onSecondaryContainer = Color(0xFFE0E0FF),
    surface            = VaultSurfaceDark,
    onSurface          = Color(0xFFE4E5F5),
    surfaceVariant     = VaultSurfaceContainer,
    onSurfaceVariant   = Color(0xFFC0C2DA),
    background         = VaultNight,
    onBackground       = Color(0xFFE4E5F5),
    error              = Color(0xFFFF6B6B),
    onError            = Color(0xFF4A0000),
    outline            = Color(0xFF4A4D72),
    outlineVariant     = Color(0xFF2A2D4A)
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
