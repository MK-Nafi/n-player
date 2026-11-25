package com.nplayer.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BackgroundPrimary = Color(0xFF181818)
val BackgroundSecondary = Color(0xFF242424)
val Accent = Color(0xFF20B8AA)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0B0)

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF0A2E2B),
    secondary = Accent,
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    surface = BackgroundSecondary,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundSecondary,
    onSurfaceVariant = TextSecondary
)

/**
 * N Player uses a dark-only theme.
 * The light theme is intentionally not implemented.
 */
@Composable
fun NPlayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
