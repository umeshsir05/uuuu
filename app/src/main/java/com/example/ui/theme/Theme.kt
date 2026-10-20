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
    primary = DairyBlue80,
    onPrimary = Color(0xFF003258),
    primaryContainer = DairyBlueDark,
    onPrimaryContainer = DairyBlueContainer,
    secondary = DairyAmber80,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF633F00),
    onSecondaryContainer = DairyAmberContainer,
    background = SurfaceDark,
    surface = SurfaceDark,
    surfaceVariant = Color(0xFF22262B)
)

private val LightColorScheme = lightColorScheme(
    primary = DairyBlue,
    onPrimary = Color.White,
    primaryContainer = DairyBlueContainer,
    onPrimaryContainer = OnDairyBlueContainer,
    secondary = DairyAmber,
    onSecondary = Color.White,
    secondaryContainer = DairyAmberContainer,
    onSecondaryContainer = OnDairyAmberContainer,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = SurfaceVariant,
    outlineVariant = OutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent dairy branding
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
