package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  lightColorScheme(
    primary = MinimalTeal,
    secondary = MinimalMint,
    background = MinimalBackground,
    surface = DarkCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = MinimalText,
    onSurface = MinimalText
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MinimalTeal,
    secondary = MinimalMint,
    background = MinimalBackground,
    surface = DarkCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = MinimalText,
    onSurface = MinimalText
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to true
  dynamicColor: Boolean = false, // Force custom theme colors
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
