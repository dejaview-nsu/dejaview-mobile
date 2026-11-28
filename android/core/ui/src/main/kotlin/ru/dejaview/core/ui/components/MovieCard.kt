package ru.dejaview.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.R
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.model.MovieSummary

private val POSTER_WIDTH = 56.dp
private val HIGHLIGHTED_POSTER_WIDTH = 72.dp
private const val POSTER_ASPECT_RATIO = 2f / 3f

/** Provides the accessibility label for the image rendered by [poster]. */
@Composable
fun MovieCard(
    movie: MovieSummary,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    overviewMaxLines: Int = Int.MAX_VALUE,
    poster: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = DejaViewShapes.card,
        color = if (highlighted) DejaViewColors.paper else DejaViewColors.surface,
        contentColor = if (highlighted) DejaViewColors.paperText else DejaViewColors.text,
    ) {
        val clickModifier = if (onClick == null) Modifier else Modifier.clickable(role = Role.Button, onClick = onClick)
        Row(
            modifier = clickModifier.padding(DejaViewSpacing.large),
            horizontalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
            verticalAlignment = Alignment.Top,
        ) {
            MoviePoster(
                title = movie.title,
                width = if (highlighted) HIGHLIGHTED_POSTER_WIDTH else POSTER_WIDTH,
                poster = poster,
            )
            MovieDetails(movie, highlighted, Modifier.weight(1f), overviewMaxLines)
        }
    }
}

@Composable
private fun MoviePoster(
    title: String,
    width: Dp,
    poster: (@Composable () -> Unit)?,
) {
    val description =
        if (poster == null) {
            stringResource(R.string.dv_movie_missing_poster)
        } else {
            stringResource(R.string.dv_movie_poster, title)
        }

    Box(
        modifier =
            Modifier
                .width(width)
                .aspectRatio(POSTER_ASPECT_RATIO)
                .clip(DejaViewShapes.poster)
                .background(DejaViewColors.elevated)
                .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (poster == null) {
            Icon(painterResource(R.drawable.dv_poster_placeholder), null, tint = DejaViewColors.mutedText)
        } else {
            poster()
        }
    }
}

@Composable
private fun MovieDetails(
    movie: MovieSummary,
    highlighted: Boolean,
    modifier: Modifier,
    overviewMaxLines: Int,
) {
    val secondaryColor = if (highlighted) DejaViewColors.paperSecondaryText else DejaViewColors.secondaryText
    Column(modifier, verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.extraSmall)) {
        if (highlighted) {
            Text(stringResource(R.string.dv_movie_best_match), style = DejaViewType.overline, color = secondaryColor)
        }
        Text(movie.title, style = if (highlighted) DejaViewType.subheading else DejaViewType.title)
        movie.year?.let { Text(it.toString(), style = DejaViewType.bodySmall, color = secondaryColor) }
        movie.overview?.takeIf(String::isNotBlank)?.let {
            Text(it, style = DejaViewType.bodySmall, color = secondaryColor, maxLines = overviewMaxLines, overflow = TextOverflow.Ellipsis)
        }
    }
}
