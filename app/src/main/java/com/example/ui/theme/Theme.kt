package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MacaronColorScheme = lightColorScheme(
    primary = MacaronPink,
    onPrimary = Color.White,
    primaryContainer = MacaronPinkLight,
    onPrimaryContainer = TextDarkCocoa,
    secondary = MacaronMint,
    onSecondary = Color.White,
    secondaryContainer = MacaronMintLight,
    onSecondaryContainer = TextDarkCocoa,
    tertiary = MacaronButter,
    onTertiary = TextDarkCocoa,
    tertiaryContainer = MacaronButterLight,
    onTertiaryContainer = TextDarkCocoa,
    background = SkeuoBezelBg,
    onBackground = TextDarkCocoa,
    surface = SkeuoSurface,
    onSurface = TextDarkCocoa,
    surfaceVariant = MacaronPinkLight,
    onSurfaceVariant = TextMutedCocoa,
    outline = SkeuoBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Keep bright pastel macaron styling as requested by user
    MaterialTheme(
        colorScheme = MacaronColorScheme,
        typography = Typography,
        content = content
    )
}
