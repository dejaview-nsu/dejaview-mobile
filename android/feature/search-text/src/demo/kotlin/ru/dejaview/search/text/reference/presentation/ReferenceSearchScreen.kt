package ru.dejaview.search.text.reference.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import ru.dejaview.core.ui.components.ContentFeedback
import ru.dejaview.core.ui.components.DejaViewTextField
import ru.dejaview.core.ui.components.FeedbackKind
import ru.dejaview.core.ui.components.MovieCard
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.feature.searchtext.R

@Composable
internal fun ReferenceSearchScreen(
    state: ReferenceSearchState,
    onAction: (ReferenceSearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val submit = {
        focus.clearFocus()
        keyboard?.hide()
        onAction(ReferenceSearchAction.Submit)
    }
    Column(modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = DejaViewSpacing.large),
            verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
        ) {
            Text(stringResource(R.string.reference_search_title), style = DejaViewType.title)
            Text(
                stringResource(R.string.reference_search_heading),
                modifier = Modifier.semantics { heading() },
                style = DejaViewType.heading,
            )
            Text(
                stringResource(R.string.reference_search_explanation),
                style = DejaViewType.body,
                color = DejaViewColors.secondaryText,
            )
            DejaViewTextField(
                value = state.query,
                onValueChange = { onAction(ReferenceSearchAction.Edit(it)) },
                label = stringResource(R.string.reference_search_description),
                placeholder = stringResource(R.string.reference_search_example),
                isError = state.characterCount > QUERY_LIMIT,
                supportingText = {
                    Text(
                        stringResource(R.string.reference_search_count, state.characterCount, QUERY_LIMIT),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                enabled = state.status != SearchStatus.Loading,
                minLines = 6,
                maxLines = 6,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            SearchFeedback(state.status, onAction)
        }
        if (state.status == SearchStatus.Editing || state.status is SearchStatus.Results) {
            PrimaryButton(
                text = stringResource(R.string.reference_search_submit),
                onClick = submit,
                modifier = Modifier.fillMaxWidth().padding(vertical = DejaViewSpacing.small),
                enabled = state.canSubmit,
            )
        }
    }
}

@Composable
private fun SearchFeedback(
    status: SearchStatus,
    onAction: (ReferenceSearchAction) -> Unit,
) {
    val retry = { onAction(ReferenceSearchAction.Submit) }
    val edit = { onAction(ReferenceSearchAction.Cancel) }
    when (status) {
        SearchStatus.Editing -> Unit
        SearchStatus.Loading -> ContentFeedback(FeedbackKind.Loading, onAction = edit)
        SearchStatus.Empty -> ContentFeedback(FeedbackKind.Empty, onAction = edit)
        SearchStatus.Offline -> ContentFeedback(FeedbackKind.Offline, onAction = retry)
        SearchStatus.Failed -> ContentFeedback(FeedbackKind.Error, onAction = retry)
        is SearchStatus.Results ->
            status.movies.forEachIndexed { index, movie ->
                MovieCard(movie = movie, highlighted = index == 0)
            }
    }
}
