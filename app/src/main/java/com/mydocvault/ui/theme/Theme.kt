package com.mydocvault.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = VaultBlue,
    onPrimary = Color.White,
    primaryContainer = VaultBlueContainer,
    onPrimaryContainer = Color(0xFF001947),
    secondary = VaultCyan,
    onSecondary = Color.White,
    secondaryContainer = VaultCyanContainer,
    onSecondaryContainer = Color(0xFF001F28),
    surface = VaultSurfaceLight,
    onSurface = VaultOnSurfaceLight,
    onSurfaceVariant = VaultOnSurfaceVariantLight,
    surfaceVariant = VaultSandMid,
    surfaceContainer = VaultSandMid,
    surfaceContainerHigh = VaultSurfaceContainerLight,
    background = VaultSand,
    onBackground = VaultOnSurfaceLight,
    outline = VaultOutlineLight,
    outlineVariant = VaultOutlineVariantLight,
    error = VaultErrorLight,
    onError = Color.White,
    errorContainer = VaultErrorContainerLight,
    onErrorContainer = Color(0xFF410002)
)

private val DarkColors = darkColorScheme(
    primary = VaultCyan,
    onPrimary = VaultOnPrimaryDark,
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = VaultCyanContainer,
    secondary = Color(0xFF5BB9E0),
    onSecondary = Color(0xFF00344A),
    secondaryContainer = Color(0xFF004D69),
    onSecondaryContainer = Color(0xFFB9EAFF),
    surface = VaultSurfaceDark,
    onSurface = VaultOnSurfaceDark,
    onSurfaceVariant = VaultOnSurfaceVariantDark,
    surfaceVariant = VaultSurfaceContainerDark,
    surfaceContainer = VaultSurfaceContainerDark,
    surfaceContainerHigh = VaultSurfaceHighDark,
    background = VaultNight,
    onBackground = VaultOnSurfaceDark,
    outline = VaultOutlineDark,
    outlineVariant = VaultOutlineVariantDark,
    error = VaultErrorDark,
    onError = VaultErrorContainerDark,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = VaultErrorContainerLight
)

private val VaultShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
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
        shapes = VaultShapes,
        content = content
    )
}
