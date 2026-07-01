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
  darkColorScheme(
    primary = MinimalTeal,
    secondary = MinimalMint,
    background = Color(0xFF0F172A), // Elegant slate dark background
    surface = Color(0xFF1E293B), // Elegant slate dark card background
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF9FAFB),
    onSurface = Color(0xFFF9FAFB)
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
  darkTheme: Boolean = false, // Default to false
  dynamicColor: Boolean = false, // Force custom theme colors
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
