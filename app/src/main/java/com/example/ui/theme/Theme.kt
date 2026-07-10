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

private val ImmersiveDarkColorScheme =
  darkColorScheme(
    primary = ImmersiveCyan,
    onPrimary = ImmersiveBgDark,
    primaryContainer = ImmersiveCardBg,
    onPrimaryContainer = ImmersiveTextPrimary,
    secondary = ImmersiveIndigo,
    onSecondary = ImmersiveBgDark,
    tertiary = ImmersivePurple,
    background = ImmersiveBgDark,
    onBackground = ImmersiveTextPrimary,
    surface = ImmersiveSurfaceDark,
    onSurface = ImmersiveTextPrimary,
    surfaceVariant = ImmersiveCardBg,
    onSurfaceVariant = ImmersiveTextSecondary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = ImmersiveDarkColorScheme,
    typography = Typography,
    content = content
  )
}
