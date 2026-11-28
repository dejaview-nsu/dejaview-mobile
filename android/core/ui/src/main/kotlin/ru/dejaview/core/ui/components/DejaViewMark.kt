package ru.dejaview.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import ru.dejaview.core.ui.theme.DejaViewColors

@Composable
fun DejaViewMark(
    modifier: Modifier = Modifier,
    outlineReveal: Float = 1f,
    frameReveal: Float = 1f,
    playReveal: Float = 1f,
) {
    Canvas(modifier) {
        val unit = size.minDimension
        drawRoundRect(
            color = DejaViewColors.paper,
            topLeft = Offset(unit * .05f, unit * .27f),
            size = Size(unit * .66f * outlineReveal, unit * .66f),
            cornerRadius = CornerRadius(unit * .16f),
            style = Stroke(unit * .045f),
            alpha = outlineReveal,
        )
        drawRoundRect(
            color = DejaViewColors.accent,
            topLeft = Offset(unit * (.36f - .18f * (1f - frameReveal)), unit * .05f),
            size = Size(unit * .60f, unit * .60f),
            cornerRadius = CornerRadius(unit * .15f),
            alpha = frameReveal,
        )
        drawPath(
            Path().apply {
                moveTo(unit * .59f, unit * .23f)
                lineTo(unit * .59f, unit * .47f)
                lineTo(unit * .77f, unit * .35f)
                close()
            },
            color = DejaViewColors.onAccent,
            alpha = playReveal,
        )
    }
}
