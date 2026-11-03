package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VnaDarkColorScheme = darkColorScheme(
    primary = VnaYellow,
    onPrimary = VnaTextDark,
    primaryContainer = VnaYellowDark,
    onPrimaryContainer = VnaTextDark,
    secondary = VnaGreen,
    onSecondary = VnaTextDark,
    background = VnaNavyBackground,
    onBackground = VnaTextPrimary,
    surface = VnaNavySurface,
    onSurface = VnaTextPrimary,
    surfaceVariant = VnaNavyCard,
    onSurfaceVariant = VnaTextSecondary,
    outline = VnaBorder,
    error = VnaRed,
    onError = VnaTextPrimary
)

@Composable
fun VnaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VnaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
