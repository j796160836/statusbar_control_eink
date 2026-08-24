package com.johnny.statusbar_control_eink.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// E-ink displays are white-background, high-contrast, and often strictly
// black-and-white — always force the light scheme and never use dynamic
// (wallpaper-derived) color, regardless of system theme/Android version.
private val EinkColorScheme = lightColorScheme(
    primary = EinkBlack,
    onPrimary = EinkWhite,
    secondary = EinkBlack,
    onSecondary = EinkWhite,
    tertiary = EinkBlack,
    onTertiary = EinkWhite,
    background = EinkWhite,
    onBackground = EinkBlack,
    surface = EinkWhite,
    onSurface = EinkBlack,
    surfaceVariant = EinkWhite,
    onSurfaceVariant = EinkBlack,
    outline = EinkBlack,
    outlineVariant = EinkGray
)

@Composable
fun Statusbar_control_einkTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EinkColorScheme,
        typography = Typography,
        shapes = EinkShapes,
        content = content
    )
}
