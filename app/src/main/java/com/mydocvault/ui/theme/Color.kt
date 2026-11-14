package com.mydocvault.ui.theme

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp

/** A diffuse surface behind the vault index and setup screens, derived from the active theme. */
@Composable
internal fun Modifier.vaultWash(): Modifier {
    val colors = MaterialTheme.colorScheme
    return background(Brush.linearGradient(listOf(
        colors.primaryContainer,
        colors.background,
        lerp(colors.surface, colors.tertiaryContainer, 0.45f)
    )))
}
