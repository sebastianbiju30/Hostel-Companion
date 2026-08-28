package com.example.hostel_companion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlueLight,
    onPrimary = TextPrimary,
    primaryContainer = AccentBlue,
    onPrimaryContainer = TextPrimary,
    secondary = AccentAmber,
    onSecondary = TextPrimary,
    secondaryContainer = DarkCardSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentTeal,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkCardSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCardSurfaceVariant,
    onSurfaceVariant = TextMuted,
    error = AccentRed,
    onError = TextPrimary
)

@Composable
fun Hostel_CompanionTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}