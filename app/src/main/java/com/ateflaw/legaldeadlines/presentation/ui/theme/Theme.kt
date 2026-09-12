package com.ateflaw.legaldeadlines.presentation.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContextWrapper
import android.content.res.Resources
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
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
    onSecondary = OnSecondaryGold,
    background = BackgroundDark,
    onBackground = OnBackgroundLight,
    surface = SurfaceDark,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnBackgroundLight
)

@SuppressLint("LocalContextGetResources", "LocalContextResourcesRead")
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
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isInspection = LocalInspectionMode.current
    val effectiveContext = if (isInspection) {
        remember(context, configuration) {
            object : ContextWrapper(context) {
                @Suppress("DEPRECATION")
                private val safeResources: Resources by lazy {
                    val base = context.resources
                    object : Resources(base.assets, base.displayMetrics, configuration) {
                        override fun getText(id: Int): CharSequence {
                            return try {
                                super.getText(id)
                            } catch (_: NotFoundException) {
                                ""
                            }
                        }

                        override fun getText(id: Int, def: CharSequence?): CharSequence {
                            return try {
                                super.getText(id, def)
                            } catch (_: NotFoundException) {
                                def ?: ""
                            }
                        }

                        override fun getString(id: Int): String {
                            return try {
                                super.getString(id)
                            } catch (_: NotFoundException) {
                                ""
                            }
                        }

                        override fun getString(id: Int, vararg formatArgs: Any?): String {
                            return try {
                                super.getString(id, *formatArgs)
                            } catch (_: NotFoundException) {
                                ""
                            }
                        }
                    }
                }

                override fun getResources(): Resources = safeResources
            }
        }
    } else {
        context
    }

    // Force Arabic RTL globally and safely wrap resources in inspection (preview) mode to prevent Resources$NotFoundException
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalContext provides effectiveContext
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
