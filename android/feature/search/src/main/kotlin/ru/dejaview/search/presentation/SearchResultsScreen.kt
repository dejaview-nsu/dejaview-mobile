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
import androidx.compose.ui.res.stringResource
import ru.dejaview.core.ui.components.ContentFeedback
import ru.dejaview.core.ui.components.FeedbackKind
import ru.dejaview.core.ui.components.MovieCard
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
) {
    val back = stringResource(R.string.search_return)
    LazyColumn(
        Modifier.fillMaxSize().padding(vertical = DejaViewSpacing.large),
        verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
    ) {
        when {
            restoring -> item { ContentFeedback(FeedbackKind.Loading, onAction = onReturn) }
            completion == null ->
                item {
                    ContentFeedback(
                        FeedbackKind.Error,
                        title = stringResource(R.string.search_result_unavailable),
                        message = stringResource(R.string.search_repeat_message),
                        actionText = back,
                        onAction = onReturn,
                    )
                }
            completion.results.isEmpty() ->
                item { ContentFeedback(FeedbackKind.Empty, actionText = back, onAction = onReturn) }
            else -> {
                val title = if (completion.mode == SearchMode.TEXT) R.string.search_results else R.string.search_movie_found
                item { Text(stringResource(title), style = DejaViewType.heading) }
                itemsIndexed(completion.results) { index, movie ->
                    MovieCard(movie, highlighted = index == 0, onClick = { onMovie(movie.movieId) })
                }
                item {
                    TextButton(onClick = onReturn) {
                        Text(stringResource(R.string.search_wrong_movie))
                    }
                }
            }
        }
    }
}
