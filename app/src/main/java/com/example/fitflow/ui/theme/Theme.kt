package com.example.fitflow.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    background = BackgroundDark,
    surface    = CardDark,
    primary    = AccentNeon,
    secondary  = SecondaryBlue,
    onBackground = TextDim,
    onSurface    = TextDim,
    onPrimary    = BackgroundDark
)

private val LightColorScheme = lightColorScheme(
    background = BackgroundLight,
    surface    = CardLight,
    primary    = AccentNeon,
    secondary  = SecondaryBlue,
    onBackground = TextDarkMode,
    onSurface    = TextDarkMode,
    onPrimary    = BackgroundLight
)

@Composable
fun FitflowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
