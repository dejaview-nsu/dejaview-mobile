package ru.dejaview.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.components.DejaViewMark
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType

private const val SPLASH_END_MS = 2600

@Composable
internal fun OpeningSplash(onContinue: () -> Unit) {
    val animation = remember { Animatable(0f) }
    val continuation = rememberUpdatedState(onContinue)
    LaunchedEffect(Unit) {
        animation.animateTo(SPLASH_END_MS.toFloat(), tween(SPLASH_END_MS, easing = LinearEasing))
        continuation.value()
    }
    Box(Modifier.fillMaxSize().clickable(onClick = onContinue)) {
        SplashFrames()
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.medium),
        ) {
            val elapsed = animation.value
            DejaViewMark(
                Modifier.size(96.dp),
                outlineReveal = reveal(elapsed, 0, 1000),
                frameReveal = reveal(elapsed, 1000, 1250),
                playReveal = reveal(elapsed, 1250, 1500),
            )
            Text(
                stringResource(R.string.app_name),
                Modifier.graphicsLayer { alpha = reveal(elapsed, 1500, 2200) },
                style = DejaViewType.display,
            )
            Text(
                stringResource(R.string.splash_tagline),
                Modifier.graphicsLayer { alpha = reveal(elapsed, 2200, 2600) },
                style = DejaViewType.bodySmall,
                color = DejaViewColors.secondaryText,
            )
        }
        Text(
            stringResource(R.string.splash_credit),
            Modifier.align(Alignment.BottomCenter).padding(bottom = DejaViewSpacing.doubleLarge),
            style = DejaViewType.caption,
            color = DejaViewColors.mutedText,
        )
    }
}

@Composable
private fun SplashFrames() {
    Canvas(Modifier.fillMaxSize()) {
        val width = size.width
        val offsets = listOf(Offset(-.15f, .15f), Offset(.08f, .08f), Offset(.57f, .59f))
        for (offset in offsets) {
            drawRoundRect(
                color = DejaViewColors.border,
                topLeft = Offset(offset.x * width, offset.y * size.height),
                size = Size(width * .76f, width * .76f),
                cornerRadius = CornerRadius(width * .07f),
                style = Stroke(1.dp.toPx()),
                alpha = .18f,
            )
        }
    }
}

private fun reveal(
    elapsed: Float,
    start: Int,
    end: Int,
): Float = ((elapsed - start) / (end - start)).coerceIn(0f, 1f)
