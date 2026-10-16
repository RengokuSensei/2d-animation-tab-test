package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = StudioAccentCyan,
    secondary = StudioAccentAmber,
    tertiary = StudioAccentMint,
    background = StudioDarkBg,
    surface = StudioSurface,
    surfaceVariant = StudioSurfaceVariant,
    onBackground = StudioOnSurface,
    onSurface = StudioOnSurface,
    outline = StudioOutline
  )

private val LightColorScheme =
  darkColorScheme(
    primary = StudioAccentCyan,
    secondary = StudioAccentAmber,
    tertiary = StudioAccentMint,
    background = StudioDarkBg,
    surface = StudioSurface,
    surfaceVariant = StudioSurfaceVariant,
    onBackground = StudioOnSurface,
    onSurface = StudioOnSurface,
    outline = StudioOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = DarkColorScheme, typography = Typography, content = content)
}
