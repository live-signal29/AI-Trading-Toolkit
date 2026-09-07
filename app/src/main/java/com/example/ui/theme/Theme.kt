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

private val DarkColorScheme = darkColorScheme(
    primary = BullishGreen,
    onPrimary = Color.Black,
    primaryContainer = SlateCardElevated,
    onPrimaryContainer = BullishGreenLight,
    secondary = AccentCyan,
    onSecondary = Color.Black,
    secondaryContainer = SlateCardElevated,
    onSecondaryContainer = AccentCyan,
    tertiary = AccentGold,
    onTertiary = Color.Black,
    error = BearishRed,
    onError = Color.White,
    background = ObsidianDark,
    onBackground = DarkTextPrimary,
    surface = SlateCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = SlateCardElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = SlateBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF059669),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF075985),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    error = BearishRed,
    onError = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek fintech dark theme
    dynamicColor: Boolean = false, // Keep high-contrast trading theme intentional
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

