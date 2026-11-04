package com.mydocvault.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary            = Color(0xFF245B4B),
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFD3EADB),
    onPrimaryContainer = Color(0xFF102E20),
    secondary          = Color(0xFF80513A),
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFF5DEC6),
    onSecondaryContainer = Color(0xFF3D261A),
    surface            = Color(0xFFFFFDF7),
    onSurface          = Color(0xFF203028),
    surfaceVariant     = Color(0xFFE8EBDF),
    onSurfaceVariant   = Color(0xFF4D5C51),
    background         = Color(0xFFF8F7F0),
    onBackground       = Color(0xFF203028),
    error              = ErrorRed,
    onError            = OnErrorLight,
    outline            = Color(0xFF768278),
    outlineVariant     = Color(0xFFCBD2C5)
)

private val DarkColors = darkColorScheme(
    primary            = Color(0xFFA0D4BA),
    onPrimary          = Color(0xFF123829),
    primaryContainer   = Color(0xFF294F3E),
    onPrimaryContainer = Color(0xFFD3EADB),
    secondary          = Color(0xFFE7B89E),
    onSecondary        = Color(0xFF462B1E),
    secondaryContainer = Color(0xFF573B2D),
    onSecondaryContainer = Color(0xFFF5DEC6),
    surface            = Color(0xFF19241F),
    onSurface          = Color(0xFFE0E9DE),
    surfaceVariant     = Color(0xFF29362F),
    onSurfaceVariant   = Color(0xFFBDCABE),
    background         = Color(0xFF121C17),
    onBackground       = Color(0xFFE0E9DE),
    error              = Color(0xFFFF6B6B),
    onError            = Color(0xFF4A0000),
    outline            = Color(0xFF86958A),
    outlineVariant     = Color(0xFF39483D)
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
