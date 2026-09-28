package com.trigger.overlay.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// iOS 17 Color Palette
private val IOSBlue = Color(0xFF007AFF)
private val IOSBackgroundLight = Color(0xFFF2F2F7)
private val IOSCardLight = Color(0xFFFFFFFF)
private val IOSSeparatorLight = Color(0xFFC6C6C8)
private val IOSLabelLight = Color(0xFF000000)
private val IOSSecondaryLabelLight = Color(0xFF8E8E93)

private val IOSBackgroundDark = Color(0xFF000000)
private val IOSCardDark = Color(0xFF1C1C1E)
private val IOSLabelDark = Color(0xFFFFFFFF)

private val LightColorScheme = lightColorScheme(
    primary = IOSBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5F0FF),
    secondary = Color(0xFF5856D6),
    background = IOSBackgroundLight,
    surface = IOSCardLight,
    surfaceVariant = Color(0xFFE5E5EA),
    onSurface = IOSLabelLight,
    onSurfaceVariant = IOSSecondaryLabelLight,
    outline = IOSSeparatorLight,
    error = Color(0xFFFF3B30)
)

private val DarkColorScheme = darkColorScheme(
    primary = IOSBlue,
    onPrimary = Color.White,
    background = IOSBackgroundDark,
    surface = IOSCardDark,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurface = IOSLabelDark,
    onSurfaceVariant = Color(0xFF98989D),
    outline = Color(0xFF38383A)
)

@Composable
fun TriggerIOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            headlineLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                letterSpacing = 0.37.sp
            ),
            headlineMedium = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                letterSpacing = 0.36.sp
            ),
            titleLarge = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                letterSpacing = 0.38.sp
            ),
            bodyLarge = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 17.sp,
                letterSpacing = (-0.41).sp
            ),
            bodyMedium = TextStyle(
                fontSize = 15.sp,
                letterSpacing = (-0.24).sp
            ),
            labelLarge = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp
            )
        ),
        content = content
    )
}
