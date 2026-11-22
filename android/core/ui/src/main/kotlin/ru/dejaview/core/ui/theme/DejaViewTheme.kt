package ru.dejaview.core.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object DejaViewColors {
    val background = Color(0xFF15100C)
    val surface = Color(0xFF221812)
    val elevated = Color(0xFF2E211A)
    val navigation = Color(0xFF1B140F)
    val scrim = Color(0xFF0A0705)
    val border = Color(0xFF3E2E24)
    val strongBorder = Color(0xFF4A372A)
    val text = Color(0xFFF3EAD3)
    val secondaryText = Color(0xFFBFAE96)
    val mutedText = Color(0xFFA39079)
    val accent = Color(0xFFE8AE3A)
    val accentHighlight = Color(0xFFF1C25F)
    val onAccent = Color(0xFF1B130E)
    val accentTint = Color(0xFF3A2A1C)
    val recording = Color(0xFFD9653B)
    val error = Color(0xFFEE7A5A)
    val success = Color(0xFF9CC27A)
    val paper = Color(0xFFEFE3C8)
    val paperText = Color(0xFF241812)
    val paperSecondaryText = Color(0xFF6B5B4E)
    val paperLink = Color(0xFF7A4F08)
}

object DejaViewSpacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val screen = 20.dp
    val extraLarge = 24.dp
    val doubleLarge = 32.dp
    val section = 40.dp
}

object DejaViewShapes {
    val poster = RoundedCornerShape(8.dp)
    val field = RoundedCornerShape(14.dp)
    val card = RoundedCornerShape(20.dp)
    val sheet = RoundedCornerShape(28.dp)
    val pill = CircleShape
    internal val outlineWidth = 1.dp
}

private val colors =
    with(DejaViewColors) {
        darkColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = accentTint,
            onPrimaryContainer = accentHighlight,
            secondary = secondaryText,
            onSecondary = background,
            secondaryContainer = elevated,
            onSecondaryContainer = DejaViewColors.text,
            tertiary = paper,
            onTertiary = paperText,
            tertiaryContainer = paper,
            onTertiaryContainer = paperText,
            background = background,
            onBackground = DejaViewColors.text,
            surface = surface,
            onSurface = DejaViewColors.text,
            surfaceVariant = elevated,
            onSurfaceVariant = secondaryText,
            surfaceTint = accent,
            surfaceContainerLowest = background,
            surfaceContainerLow = navigation,
            surfaceContainer = surface,
            surfaceContainerHigh = elevated,
            surfaceContainerHighest = accentTint,
            outline = mutedText,
            outlineVariant = border,
            inverseSurface = paper,
            inverseOnSurface = paperText,
            inversePrimary = paperLink,
            error = error,
            onError = background,
            errorContainer = elevated,
            onErrorContainer = error,
            scrim = scrim,
        )
    }

private val typography =
    with(DejaViewType) {
        Typography(
            displayLarge = display,
            displayMedium = headline,
            displaySmall = heading,
            headlineLarge = headline,
            headlineMedium = heading,
            headlineSmall = subheading,
            titleLarge = subheading,
            titleMedium = title,
            titleSmall = label,
            bodyLarge = body,
            bodyMedium = bodyMedium,
            bodySmall = bodySmall,
            labelLarge = button,
            labelMedium = bodySmallMedium,
            labelSmall = caption,
        )
    }

private val shapes =
    with(DejaViewShapes) {
        Shapes(extraSmall = poster, small = field, medium = card, large = sheet, extraLarge = sheet)
    }

@Composable
fun DejaViewTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, shapes = shapes, content = content)
}
