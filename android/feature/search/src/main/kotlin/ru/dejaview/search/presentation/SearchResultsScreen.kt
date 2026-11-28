package ru.dejaview.search.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ru.dejaview.core.ui.components.FeedbackKind
import ru.dejaview.core.ui.components.MovieCard
import ru.dejaview.core.ui.components.ScreenHeader
import ru.dejaview.core.ui.components.SearchLoadingScreen
import ru.dejaview.core.ui.components.SearchStatusPage
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.feature.search.R
import ru.dejaview.model.MovieId
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode

@Composable
fun SearchResultsScreen(
    completion: SearchCompletion?,
    restoring: Boolean,
    onReturn: () -> Unit,
    onMovie: (MovieId) -> Unit,
    onChooseMode: () -> Unit,
) {
    if (restoring) {
        SearchLoadingScreen(onCancel = onReturn)
        return
    }
    if (completion == null) {
        SearchStatusPage(
            FeedbackKind.Error,
            stringResource(R.string.search_results_header),
            onPrimary = onReturn,
            onSecondary = onChooseMode,
            title = stringResource(R.string.search_result_unavailable),
            message = stringResource(R.string.search_repeat_message),
            primary = stringResource(R.string.search_return),
        )
        return
    }
    if (completion.results.isEmpty()) {
        EmptySearchResults(completion, onReturn, onChooseMode)
        return
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(vertical = DejaViewSpacing.large),
        verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
    ) {
        item { MatchedResultsHeader(completion, onReturn) }
        if (completion.mode == SearchMode.TEXT) {
            item { QuerySummary(completion.inputSummary, onReturn) }
        }
        itemsIndexed(completion.results) { index, movie ->
            if (completion.mode == SearchMode.TEXT && index > 0) {
                TextResultRow(index + 1, movie, onClick = { onMovie(movie.movieId) })
            } else {
                val url = movie.posterUrl.httpsPoster()
                val poster: (@Composable () -> Unit)? = if (url == null) null else ({ ResultPoster(url) })
                MovieCard(
                    movie,
                    highlighted = index == 0,
                    onClick = { onMovie(movie.movieId) },
                    overviewMaxLines = if (completion.mode == SearchMode.TEXT) 2 else 3,
                    poster = poster,
                )
            }
        }
        item {
            TextButton(onClick = onReturn) {
                Text(stringResource(R.string.search_wrong_movie))
            }
        }
    }
}

@Composable
private fun MatchedResultsHeader(
    completion: SearchCompletion,
    onReturn: () -> Unit,
) {
    if (completion.mode == SearchMode.TEXT) {
        val count = completion.results.size
        ScreenHeader(pluralStringResource(R.plurals.search_match_count, count, count), onReturn)
    } else {
        ScreenHeader(stringResource(R.string.search_movie_found), onReturn, close = true)
    }
}

@Composable
internal fun QuerySummary(
    query: String,
    onEdit: () -> Unit,
) {
    androidx.compose.material3.AssistChip(
        onClick = onEdit,
        label = { Text(query, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        trailingIcon = { Text(stringResource(R.string.search_edit_query), style = DejaViewType.caption) },
    )
}
