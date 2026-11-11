package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = ForgeAmberPrimary,
    onPrimary = Color.Black,
    primaryContainer = ForgeAmberDark,
    onPrimaryContainer = Color.White,
    secondary = ForgeCyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = ForgeCyanMuted,
    onSecondaryContainer = Color.White,
    background = ObsidianDarkBg,
    onBackground = TextHighDark,
    surface = ObsidianSurface,
    onSurface = TextHighDark,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = TextMutedDark,
    outline = ObsidianBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ForgeAmberDark,
    onPrimary = Color.White,
    primaryContainer = ForgeAmberLight,
    onPrimaryContainer = Color.Black,
    secondary = ForgeCyanMuted,
    onSecondary = Color.White,
    secondaryContainer = ForgeCyanAccent,
    onSecondaryContainer = Color.Black,
    background = StudioLightBg,
    onBackground = TextHighLight,
    surface = StudioSurface,
    onSurface = TextHighLight,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = TextMutedLight,
    outline = StudioBorder
)

@Composable
fun HephaistosTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colors = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
