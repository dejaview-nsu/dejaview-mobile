package ru.dejaview.search.presentation

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.model.MovieSummary

@Composable
internal fun TextResultRow(
    number: Int,
    movie: MovieSummary,
    onClick: () -> Unit,
) {
    Column {
        Row(
            Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(vertical = DejaViewSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(DejaViewSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(number.toString(), color = DejaViewColors.mutedText, style = DejaViewType.caption)
            Box(
                Modifier
                    .width(46.dp)
                    .aspectRatio(.68f)
                    .clip(DejaViewShapes.poster)
                    .background(DejaViewColors.elevated),
            ) {
                ResultPoster(movie.posterUrl)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.extraSmall)) {
                Text(movie.title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = DejaViewType.title)
                val detail = listOfNotNull(movie.year?.toString(), movie.overview?.takeIf(String::isNotBlank)).joinToString(" · ")
                if (detail.isNotEmpty()) {
                    Text(
                        detail,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = DejaViewType.bodySmall,
                        color = DejaViewColors.secondaryText,
                    )
                }
            }
        }
        HorizontalDivider(color = DejaViewColors.border)
    }
}
