package ru.dejaview.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.theme.DejaViewColors

@Composable
internal fun SearchArtwork(modifier: Modifier) {
    val scan by rememberInfiniteTransition(label = "catalog scan").animateFloat(
        initialValue = .20f,
        targetValue = .76f,
        animationSpec = infiniteRepeatable(tween(1300), repeatMode = RepeatMode.Reverse),
        label = "scan line",
    )
    Canvas(modifier) {
        val cardWidth = size.width * .27f
        val cardHeight = size.height * .75f
        val corner = CornerRadius(9.dp.toPx())
        listOf(DejaViewColors.surface, DejaViewColors.accentTint, Color(0xFF233642)).forEachIndexed { index, color ->
            val center = Offset(size.width * (.21f + index * .29f), size.height * .52f)
            rotate((index - 1) * 9f, center) {
                drawRoundRect(
                    color,
                    topLeft = Offset(center.x - cardWidth / 2, center.y - cardHeight / 2),
                    size = Size(cardWidth, cardHeight),
                    cornerRadius = corner,
                )
            }
        }
        val y = size.height * scan
        drawLine(DejaViewColors.accent, Offset.Zero.copy(y = y), Offset(size.width, y), strokeWidth = 2.dp.toPx())
    }
}
