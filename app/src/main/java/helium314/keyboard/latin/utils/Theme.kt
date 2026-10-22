// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.utils

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import helium314.keyboard.latin.R

/**
 * Material 3 (Material You) theme for the settings UI, with the more rounded shapes and
 * bolder headlines of the M3 Expressive style.
 * On Android 12+ the wallpaper based dynamic colors are used, on older versions a full tonal
 * scheme is derived from the accent color.
 */
@Composable
fun Theme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val baseScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(LocalContext.current)
        else dynamicLightColorScheme(LocalContext.current)
    } else {
        schemeFromSeed(colorResource(R.color.accent), dark)
    }
    // tinted screen background in light mode, so the grouped setting cards (surfaceBright) stand out
    val colorScheme = if (dark) baseScheme else baseScheme.copy(background = baseScheme.surfaceContainer)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = expressiveTypography,
        shapes = expressiveShapes,
        content = content
    )
}

private val material3 = Typography()

private val expressiveTypography = Typography(
    displayLarge = material3.displayLarge.copy(fontWeight = FontWeight.Medium),
    displayMedium = material3.displayMedium.copy(fontWeight = FontWeight.Medium),
    displaySmall = material3.displaySmall.copy(fontWeight = FontWeight.Medium),
    headlineLarge = material3.headlineLarge.copy(fontWeight = FontWeight.Medium),
    headlineMedium = material3.headlineMedium.copy(fontWeight = FontWeight.Medium),
    headlineSmall = material3.headlineSmall.copy(fontWeight = FontWeight.Medium),
    titleLarge = material3.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = material3.titleMedium.copy(fontWeight = FontWeight.Bold),
    titleSmall = material3.titleSmall.copy(fontWeight = FontWeight.Bold),
    labelLarge = material3.labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
)

private val expressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Whether the current color scheme is a dark one, independent of how the theme was chosen. */
val ColorScheme.isDark get() = surface.luminance() < 0.5f

/**
 * Simple tonal palette generation for devices without dynamic colors.
 * Hue of the seed is kept, chroma and tone are adjusted similar to Material You key colors.
 */
private fun schemeFromSeed(seed: Color, dark: Boolean): ColorScheme {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(seed.toArgb(), hsl)
    val hue = hsl[0]
    fun tone(h: Float, saturation: Float, lightness: Float) =
        Color(ColorUtils.HSLToColor(floatArrayOf((h + 360f) % 360f, saturation, lightness)))
    val secondaryHue = hue
    val tertiaryHue = hue + 60f
    return if (dark) darkColorScheme(
        primary = tone(hue, 0.75f, 0.80f),
        onPrimary = tone(hue, 0.9f, 0.18f),
        primaryContainer = tone(hue, 0.6f, 0.30f),
        onPrimaryContainer = tone(hue, 0.9f, 0.90f),
        secondary = tone(secondaryHue, 0.25f, 0.78f),
        onSecondary = tone(secondaryHue, 0.3f, 0.18f),
        secondaryContainer = tone(secondaryHue, 0.22f, 0.28f),
        onSecondaryContainer = tone(secondaryHue, 0.4f, 0.90f),
        tertiary = tone(tertiaryHue, 0.45f, 0.78f),
        onTertiary = tone(tertiaryHue, 0.5f, 0.18f),
        tertiaryContainer = tone(tertiaryHue, 0.35f, 0.30f),
        onTertiaryContainer = tone(tertiaryHue, 0.6f, 0.90f),
        background = tone(hue, 0.10f, 0.07f),
        onBackground = tone(hue, 0.10f, 0.90f),
        surface = tone(hue, 0.10f, 0.07f),
        onSurface = tone(hue, 0.10f, 0.90f),
        surfaceVariant = tone(hue, 0.12f, 0.28f),
        onSurfaceVariant = tone(hue, 0.12f, 0.80f),
        surfaceContainerLowest = tone(hue, 0.10f, 0.04f),
        surfaceContainerLow = tone(hue, 0.10f, 0.10f),
        surfaceContainer = tone(hue, 0.10f, 0.12f),
        surfaceContainerHigh = tone(hue, 0.10f, 0.16f),
        surfaceContainerHighest = tone(hue, 0.10f, 0.21f),
        surfaceBright = tone(hue, 0.10f, 0.24f),
        surfaceDim = tone(hue, 0.10f, 0.07f),
        outline = tone(hue, 0.08f, 0.56f),
        outlineVariant = tone(hue, 0.10f, 0.28f),
    ) else lightColorScheme(
        primary = tone(hue, 0.65f, 0.40f),
        onPrimary = Color.White,
        primaryContainer = tone(hue, 0.85f, 0.90f),
        onPrimaryContainer = tone(hue, 0.8f, 0.14f),
        secondary = tone(secondaryHue, 0.18f, 0.42f),
        onSecondary = Color.White,
        secondaryContainer = tone(secondaryHue, 0.40f, 0.89f),
        onSecondaryContainer = tone(secondaryHue, 0.3f, 0.14f),
        tertiary = tone(tertiaryHue, 0.35f, 0.40f),
        onTertiary = Color.White,
        tertiaryContainer = tone(tertiaryHue, 0.70f, 0.90f),
        onTertiaryContainer = tone(tertiaryHue, 0.6f, 0.14f),
        background = tone(hue, 0.30f, 0.98f),
        onBackground = tone(hue, 0.10f, 0.11f),
        surface = tone(hue, 0.30f, 0.98f),
        onSurface = tone(hue, 0.10f, 0.11f),
        surfaceVariant = tone(hue, 0.25f, 0.90f),
        onSurfaceVariant = tone(hue, 0.10f, 0.30f),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = tone(hue, 0.30f, 0.96f),
        surfaceContainer = tone(hue, 0.25f, 0.94f),
        surfaceContainerHigh = tone(hue, 0.22f, 0.92f),
        surfaceContainerHighest = tone(hue, 0.20f, 0.90f),
        surfaceBright = tone(hue, 0.30f, 0.98f),
        surfaceDim = tone(hue, 0.15f, 0.87f),
        outline = tone(hue, 0.08f, 0.47f),
        outlineVariant = tone(hue, 0.15f, 0.80f),
    )
}

const val previewDark = true
