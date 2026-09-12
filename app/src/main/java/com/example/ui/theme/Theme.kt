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
    primary = BengkelBlueCyan,
    onPrimary = Slate900,
    primaryContainer = BengkelBlueDeep,
    onPrimaryContainer = BengkelBlueLight,
    secondary = BengkelAmber,
    onSecondary = Slate900,
    secondaryContainer = BengkelAmberDark,
    onSecondaryContainer = BengkelAmberLight,
    tertiary = BengkelGreen,
    background = Slate900,
    surface = Slate800,
    surfaceVariant = Slate700,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Slate200
)

private val LightColorScheme = lightColorScheme(
    primary = BengkelBluePrimary,
    onPrimary = Color.White,
    primaryContainer = BengkelBlueLight,
    onPrimaryContainer = BengkelBlueDeep,
    secondary = BengkelAmber,
    onSecondary = Slate900,
    secondaryContainer = BengkelAmberLight,
    onSecondaryContainer = Slate900,
    tertiary = BengkelGreenDark,
    background = Slate50,
    surface = Color.White,
    surfaceVariant = Slate100,
    onBackground = Slate900,
    onSurface = Slate900,
    onSurfaceVariant = Slate600
)

@Composable
fun BengkelkuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our signature automotive branding
    content: @Composable () -> Unit
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

// Alias for backwards compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    BengkelkuTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
