package ru.dejaview.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.theme.DejaViewColors

@Composable
internal fun FilmStatusArtwork(empty: Boolean) {
    Canvas(Modifier.width(if (empty) 134.dp else 222.dp).height(if (empty) 118.dp else 94.dp)) {
        val radius = CornerRadius(9.dp.toPx())
        if (empty) {
            val card = Size(size.width * .64f, size.height * .78f)
            val front = Offset(size.width - card.width, 0f)
            drawRoundRect(DejaViewColors.border, Offset(0f, size.height - card.height), card, radius, style = Stroke(1.dp.toPx()))
            drawRoundRect(DejaViewColors.surface, front, card, radius)
            drawRoundRect(
                DejaViewColors.accent,
                front,
                card,
                radius,
                style = Stroke(1.25.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))),
            )
            repeat(3) { line ->
                val start = Offset(front.x + card.width * .26f, card.height * (.32f + line * .12f))
                drawLine(
                    DejaViewColors.accent,
                    start,
                    start.copy(x = start.x + card.width * (.48f - line * .12f)),
                    strokeWidth = 2.dp.toPx(),
                )
            }
        } else {
            val width = size.width / 6
            repeat(5) { index ->
                val left = index * (size.width - width) / 4
                val topLeft = Offset(left, 0f)
                val frame = Size(width, size.height)
                drawRoundRect(if (index == 2) DejaViewColors.accentTint else DejaViewColors.surface, topLeft, frame, radius)
                if (index == 2) drawRoundRect(DejaViewColors.accent, topLeft, frame, radius, style = Stroke(1.25.dp.toPx()))
            }
        }
    }
}
