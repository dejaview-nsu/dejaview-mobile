package ru.dejaview.audio.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.theme.DejaViewColors

@Composable
internal fun AudioWaveform(samples: List<Float>, progress: Float = 1f, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(52.dp)) {
        val center = size.height / 2
        val played = progress.coerceIn(0f, 1f)
        if (samples.isEmpty()) {
            drawLine(DejaViewColors.strongBorder, Offset(0f, center), Offset(size.width, center), 2.dp.toPx())
            drawLine(DejaViewColors.accent, Offset(0f, center), Offset(size.width * played, center), 2.dp.toPx())
        } else {
            val spacing = size.width / maxOf(samples.size, 50)
            samples.forEachIndexed { index, level ->
                val x = (index + .5f) * spacing
                val height = (level * center).coerceIn(2.dp.toPx(), center)
                drawLine(
                    if (index.toFloat() / samples.size <= played) DejaViewColors.accent else DejaViewColors.strongBorder,
                    Offset(x, center - height),
                    Offset(x, center + height),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
