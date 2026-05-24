package com.meditrack.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = Blue600,
    onPrimary        = White,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue800,
    secondary        = Teal400,
    onSecondary      = White,
    secondaryContainer = Teal50,
    onSecondaryContainer = Teal800,
    tertiary         = Purple600,
    onTertiary       = White,
    tertiaryContainer = Purple50,
    onTertiaryContainer = Purple800,
    error            = Red600,
    onError          = White,
    errorContainer   = Red50,
    onErrorContainer = Red800,
    background       = White,
    onBackground     = Black,
    surface          = White,
    onSurface        = Black,
    surfaceVariant   = Gray50,
    onSurfaceVariant = Gray600,
    outline          = Gray200
)

private val DarkColorScheme = darkColorScheme(
    primary          = Blue200,
    onPrimary        = Blue900,
    primaryContainer = Blue800,
    onPrimaryContainer = Blue50,
    secondary        = Teal200,
    onSecondary      = Teal800,
    secondaryContainer = Teal800,
    onSecondaryContainer = Teal50,
    background       = Gray900,
    onBackground     = White,
    surface          = Gray900,
    onSurface        = White,
    surfaceVariant   = Color(0xFF2A2A28),
    onSurfaceVariant = Gray200,
    outline          = Gray600
)

private val Teal200 = androidx.compose.ui.graphics.Color(0xFF5DCAA5)
private val Color = androidx.compose.ui.graphics.Color

@Composable
fun MediTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
