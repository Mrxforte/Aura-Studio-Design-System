package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AuraDarkColorScheme = darkColorScheme(
    primary = AuraCreativeAccent,
    onPrimary = Color.White,
    primaryContainer = AuraElevatedSurface,
    onPrimaryContainer = AuraAiHighlight,
    inversePrimary = AuraSecondaryAccent,
    secondary = AuraSecondaryAccent,
    onSecondary = Color.White,
    secondaryContainer = AuraToolbarSurface,
    onSecondaryContainer = AuraTextPrimary,
    tertiary = AuraCyanHighlight,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF132F38),
    onTertiaryContainer = AuraCyanHighlight,
    background = AuraDarkBackground,
    onBackground = AuraTextPrimary,
    surface = AuraPrimarySurface,
    onSurface = AuraTextPrimary,
    surfaceVariant = AuraElevatedSurface,
    onSurfaceVariant = AuraTextSecondary,
    surfaceTint = AuraCreativeAccent,
    outline = AuraBorderSubtle,
    outlineVariant = Color(0xFF262933),
    error = AuraError,
    onError = Color.White,
    errorContainer = Color(0xFF3B181B),
    onErrorContainer = AuraError
)

@Composable
fun AuraStudioTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AuraDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = AuraDarkBackground.toArgb()
                window.navigationBarColor = AuraDarkBackground.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for any existing references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AuraStudioTheme(content = content)
}
