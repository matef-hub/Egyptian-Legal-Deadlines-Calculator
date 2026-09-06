package com.ateflaw.legaldeadlines.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLegal,
    onPrimary = OnPrimaryLegal,
    primaryContainer = PrimaryContainerLegal,
    onPrimaryContainer = OnPrimaryContainerLegal,
    secondary = SecondaryGold,
    onSecondary = OnSecondaryGold,
    secondaryContainer = SecondaryContainerGold,
    onSecondaryContainer = OnSecondaryContainerGold,
    tertiary = TertiaryEmerald,
    onTertiary = OnTertiaryEmerald,
    tertiaryContainer = TertiaryContainerEmerald,
    onTertiaryContainer = OnTertiaryContainerEmerald,
    background = BackgroundWarm,
    onBackground = OnBackgroundDark,
    surface = SurfaceWarm,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantWarm,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineBorder,
    error = ErrorRed,
    onError = OnErrorRed,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLegalDark,
    onPrimary = OnPrimaryLegalDark,
    primaryContainer = PrimaryLegal,
    onPrimaryContainer = OnPrimaryLegal,
    secondary = SecondaryContainerGold,
    onSecondary = OnSecondaryContainerGold,
    background = BackgroundDark,
    onBackground = OnBackgroundLight,
    surface = SurfaceDark,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnBackgroundLight
)

@Composable
fun EgyptianLegalDeadlinesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    // Force Arabic RTL globally
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
