package com.mydocvault.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary            = Color(0xFF192830),
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFD7EBF0),
    onPrimaryContainer = Color(0xFF102D38),
    secondary          = Color(0xFF12645F),
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFCEF0E8),
    onSecondaryContainer = Color(0xFF073B37),
    surface            = Color(0xFFFEFFFF),
    onSurface          = Color(0xFF14252D),
    surfaceVariant     = Color(0xFFE7EFF1),
    onSurfaceVariant   = Color(0xFF485C64),
    background         = Color(0xFFEDF4F5),
    onBackground       = Color(0xFF14252D),
    tertiary           = Color(0xFF7A4D37),
    tertiaryContainer  = Color(0xFFF5E2D6),
    onTertiaryContainer = Color(0xFF492D20),
    error              = Color(0xFFAA342C),
    onError            = Color.White,
    outline            = Color(0xFF697E87),
    outlineVariant     = Color(0xFFC5D4D8)
)

private val DarkColors = darkColorScheme(
    primary            = Color(0xFFE4F3F5),
    onPrimary          = Color(0xFF13292F),
    primaryContainer   = Color(0xFF28434C),
    onPrimaryContainer = Color(0xFFD7EBF0),
    secondary          = Color(0xFF94D9CA),
    onSecondary        = Color(0xFF073932),
    secondaryContainer = Color(0xFF154D47),
    onSecondaryContainer = Color(0xFFCEF0E8),
    surface            = Color(0xFF18292F),
    onSurface          = Color(0xFFE0ECEE),
    surfaceVariant     = Color(0xFF24393F),
    onSurfaceVariant   = Color(0xFFB5C9CD),
    background         = Color(0xFF101D22),
    onBackground       = Color(0xFFE0ECEE),
    tertiary           = Color(0xFFE8BAA0),
    tertiaryContainer  = Color(0xFF533C30),
    onTertiaryContainer = Color(0xFFFFE4D5),
    error              = Color(0xFFFFB4AB),
    onError            = Color(0xFF4A0000),
    outline            = Color(0xFF889FA4),
    outlineVariant     = Color(0xFF354B51)
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
