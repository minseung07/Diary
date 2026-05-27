package com.diarylite.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF2F6B4F),
    onPrimary = Color.White,
    secondary = Color(0xFF51635A),
    tertiary = Color(0xFF6B5F2F),
    background = Color(0xFFFBFDF9),
    surface = Color(0xFFFBFDF9),
    surfaceVariant = Color(0xFFE0E4DD),
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF97D5AF),
    onPrimary = Color(0xFF00391F),
    secondary = Color(0xFFB9CCC0),
    tertiary = Color(0xFFD8C77B),
    background = Color(0xFF101512),
    surface = Color(0xFF101512),
    surfaceVariant = Color(0xFF404941),
)

@Composable
fun DiaryLiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
