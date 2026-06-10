package com.example.mylog.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF2F6FED),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E4FF),
    onPrimaryContainer = Color(0xFF0B2F6F),
    secondary = Color(0xFF3E7B5F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7F0E2),
    onSecondaryContainer = Color(0xFF123524),
    tertiary = Color(0xFF9C6B24),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE2B8),
    onTertiaryContainer = Color(0xFF3B2400),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF172033),
    surface = Color.White,
    onSurface = Color(0xFF172033),
    surfaceVariant = Color(0xFFE7EAF0),
    onSurfaceVariant = Color(0xFF475066),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun MylogTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
