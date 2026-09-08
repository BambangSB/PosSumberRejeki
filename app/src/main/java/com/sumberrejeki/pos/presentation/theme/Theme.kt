package com.sumberrejeki.pos.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PosBluePrimary,
    onPrimary = Color.White,
    primaryContainer = PosBlueContainer,
    onPrimaryContainer = OnPosBlueContainer,
    secondary = PosBlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = PosBlueContainer,
    onSecondaryContainer = OnPosBlueContainer,
    tertiary = AccentAmber,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = AccentAmberDark,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PosBlueSecondary,
    onPrimary = Color.White,
    primaryContainer = PosBlueDark,
    onPrimaryContainer = PosBlueContainer,
    secondary = PosBlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = PosBlueDark,
    onSecondaryContainer = PosBlueContainer,
    tertiary = AccentAmber,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = AmberContainer,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = OutlineDark,
    outlineVariant = Color(0xFF475569),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF7F1D1D)
)

@Composable
fun SumberRejekiPOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}