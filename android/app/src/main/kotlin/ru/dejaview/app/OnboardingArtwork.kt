package ru.dejaview.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType

@Composable
internal fun OnboardingArtwork(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth().heightIn(min = 174.dp, max = 278.dp), contentAlignment = Alignment.Center) {
        val cardWidth = minOf(maxWidth * .50f, 170.dp)
        OnboardingFilm(
            Modifier.align(Alignment.CenterStart).padding(start = maxWidth * .09f).width(cardWidth * .85f).rotate(-12f),
            DejaViewColors.elevated,
            Color(0xFF233642),
            stringResource(R.string.onboarding_fragment),
            DejaViewColors.secondaryText,
        )
        OnboardingFilm(
            Modifier.align(Alignment.CenterEnd).padding(end = maxWidth * .09f).width(cardWidth).rotate(11f),
            DejaViewColors.paper,
            Color(0xFF784538),
            stringResource(R.string.onboarding_film),
            DejaViewColors.paperText,
        )
    }
}

@Composable
private fun OnboardingFilm(
    modifier: Modifier,
    surface: Color,
    poster: Color,
    title: String,
    text: Color,
) {
    Column(modifier.clip(DejaViewShapes.field).background(surface).padding(DejaViewSpacing.medium)) {
        Box(Modifier.fillMaxWidth().aspectRatio(.88f).clip(DejaViewShapes.poster).background(poster))
        Spacer(Modifier.padding(top = DejaViewSpacing.small))
        Text(title, color = text, style = DejaViewType.title)
    }
}
