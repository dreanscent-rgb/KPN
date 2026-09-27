package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = KpnBlue,
    onPrimary = Color.White,
    primaryContainer = KpnBlueLight,
    onPrimaryContainer = KpnBlueDark,
    secondary = KpnBlueDark,
    onSecondary = Color.White,
    secondaryContainer = KpnBlueLight,
    onSecondaryContainer = KpnBlueDark,
    tertiary = KpnYellow,
    onTertiary = Color(0xFF1E1E1E),
    tertiaryContainer = KpnYellowLight,
    onTertiaryContainer = Color(0xFF7A5800),
    background = BgLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    error = StatusDanger,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4D94FF),
    onPrimary = Color(0xFF002B7A),
    primaryContainer = Color(0xFF003D99),
    onPrimaryContainer = Color(0xFFD9E6FF),
    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF003258),
    tertiary = KpnYellow,
    onTertiary = Color(0xFF1E1E1E),
    background = Color(0xFF0B111E),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF131D31),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
