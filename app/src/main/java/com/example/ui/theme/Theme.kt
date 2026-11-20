package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ObsidianBlack,
    onPrimary = ChampagneGold,
    primaryContainer = SoftGoldTint,
    onPrimaryContainer = ObsidianBlack,
    secondary = ChampagneGold,
    onSecondary = ObsidianBlack,
    secondaryContainer = ColorTokens.LightSecondaryContainer,
    onSecondaryContainer = InkBlack,
    tertiary = DeepGold,
    onTertiary = SilkWhite,
    background = SoftGrayBg,
    onBackground = InkBlack,
    surface = CardWhite,
    onSurface = InkBlack,
    surfaceVariant = SilkWhite,
    onSurfaceVariant = MutedSlate,
    outline = SoftBorder,
    error = CrimsonExpense,
    onError = SilkWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = ObsidianBlack,
    primaryContainer = ElevatedCharcoal,
    onPrimaryContainer = BrightChampagne,
    secondary = BrightChampagne,
    onSecondary = ObsidianBlack,
    secondaryContainer = CharcoalSurface,
    onSecondaryContainer = SilkWhite,
    tertiary = DeepGold,
    onTertiary = SilkWhite,
    background = ObsidianBlack,
    onBackground = SilkWhite,
    surface = CharcoalSurface,
    onSurface = SilkWhite,
    surfaceVariant = ElevatedCharcoal,
    onSurfaceVariant = SoftBorder,
    outline = ElevatedCharcoal,
    error = CrimsonExpense,
    onError = SilkWhite
)

private object ColorTokens {
    val LightSecondaryContainer = SoftGoldTint
}

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun DressyElegantTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
