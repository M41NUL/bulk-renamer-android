/*
 * File: Theme.kt
 * Purpose: Custom Material3 theme wrapper supporting light, dark, and system themes
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class AppColors(
    val bg: Color,
    val surface: Color,
    val surfaceGlass: Color,
    val stroke: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentPress: Color,
    val success: Color,
    val danger: Color,
    val blobA: Color,
    val blobB: Color,
    val isDark: Boolean
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        bg = LightBg,
        surface = LightSurface,
        surfaceGlass = LightSurfaceGlass,
        stroke = LightStroke,
        textPrimary = LightTextPrimary,
        textSecondary = LightTextSecondary,
        textTertiary = LightTextTertiary,
        accent = LightAccent,
        accentPress = LightAccentPress,
        success = LightSuccess,
        danger = LightDanger,
        blobA = LightBlobA,
        blobB = LightBlobB,
        isDark = false
    )
}

private val LightAppColors = AppColors(
    bg = LightBg,
    surface = LightSurface,
    surfaceGlass = LightSurfaceGlass,
    stroke = LightStroke,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    accent = LightAccent,
    accentPress = LightAccentPress,
    success = LightSuccess,
    danger = LightDanger,
    blobA = LightBlobA,
    blobB = LightBlobB,
    isDark = false
)

private val DarkAppColors = AppColors(
    bg = DarkBg,
    surface = DarkSurface,
    surfaceGlass = DarkSurfaceGlass,
    stroke = DarkStroke,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    accent = DarkAccent,
    accentPress = DarkAccentPress,
    success = DarkSuccess,
    danger = DarkDanger,
    blobA = DarkBlobA,
    blobB = DarkBlobB,
    isDark = true
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = Color.White,
    background = DarkBg,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceGlass,
    outline = DarkStroke
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = Color.White,
    background = LightBg,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceGlass,
    outline = LightStroke
)

@Composable
fun AppTheme(
    themeChoice: com.example.data.preferences.ThemeChoice,
    content: @Composable () -> Unit
) {
    val isDark = when (themeChoice) {
        com.example.data.preferences.ThemeChoice.LIGHT -> false
        com.example.data.preferences.ThemeChoice.DARK -> true
        com.example.data.preferences.ThemeChoice.SYSTEM -> isSystemInDarkTheme()
    }
    BulkRenamerTheme(darkTheme = isDark, content = content)
}

@Composable
fun BulkRenamerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: run {
                var ctx: Context = view.context
                while (ctx is ContextWrapper) {
                    if (ctx is Activity) return@run ctx.window
                    ctx = ctx.baseContext
                }
                null
            }
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
