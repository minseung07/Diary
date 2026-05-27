package com.diarylite.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF006D77),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCF2F4),
    onPrimaryContainer = Color(0xFF00363B),
    secondary = Color(0xFF5E6272),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4E7F2),
    onSecondaryContainer = Color(0xFF1B1F2D),
    tertiary = Color(0xFFA55237),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBCF),
    onTertiaryContainer = Color(0xFF3B0A00),
    background = Color(0xFFF7F9FA),
    onBackground = Color(0xFF171C1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171C1F),
    surfaceVariant = Color(0xFFE8EEF0),
    onSurfaceVariant = Color(0xFF4B5559),
    outline = Color(0xFF707A7F),
    outlineVariant = Color(0xFFD5DDE0),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF83D4DC),
    onPrimary = Color(0xFF00363B),
    primaryContainer = Color(0xFF004F57),
    onPrimaryContainer = Color(0xFFCCF2F4),
    secondary = Color(0xFFC7CAD7),
    onSecondary = Color(0xFF2F3342),
    secondaryContainer = Color(0xFF464A59),
    onSecondaryContainer = Color(0xFFE4E7F2),
    tertiary = Color(0xFFFFB59F),
    onTertiary = Color(0xFF5F1606),
    tertiaryContainer = Color(0xFF843820),
    onTertiaryContainer = Color(0xFFFFDBCF),
    background = Color(0xFF101416),
    onBackground = Color(0xFFE0E4E7),
    surface = Color(0xFF181D20),
    onSurface = Color(0xFFE0E4E7),
    surfaceVariant = Color(0xFF3F484C),
    onSurfaceVariant = Color(0xFFBFC8CC),
    outline = Color(0xFF899397),
    outlineVariant = Color(0xFF3F484C),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val DiaryShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
)

private val DiaryTypography = Typography(
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 31.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    ),
    bodyLarge = TextStyle(
        fontSize = 17.sp,
        lineHeight = 27.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
    ),
)

@Composable
fun DiaryLiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = DiaryTypography,
        shapes = DiaryShapes,
        content = content,
    )
}
