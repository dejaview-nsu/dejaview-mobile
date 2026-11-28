package ru.dejaview.search.text.reference.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import ru.dejaview.core.ui.components.ContentFeedback
import ru.dejaview.core.ui.components.DejaViewTextField
import ru.dejaview.core.ui.components.FeedbackKind
import ru.dejaview.core.ui.components.MovieCard
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.components.SearchLoadingScreen
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.feature.searchtext.R
import ru.dejaview.search.text.reference.domain.MAX_QUERY_LENGTH
import ru.dejaview.search.text.reference.domain.MIN_QUERY_LENGTH

private const val DESCRIPTION_LINES = 6

@Composable
internal fun ReferenceSearchScreen(
    state: ReferenceSearchState,
    onAction: (ReferenceSearchAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.status == SearchStatus.Loading) {
        SearchLoadingScreen(onCancel = { onAction(ReferenceSearchAction.Cancel) }, modifier = modifier.imePadding())
        return
    }
    if (state.status == SearchStatus.Offline || state.status == SearchStatus.Failed || state.status is SearchStatus.RateLimited) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SearchFeedback(
                state.status,
                onAction,
                Modifier.verticalScroll(rememberScrollState()).padding(vertical = DejaViewSpacing.large),
                secondaryAction = { TextButton(onClick = onBack) { Text(stringResource(R.string.text_search_home)) } },
            )
        }
        return
    }
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
            SearchHeading()
            DescriptionField(state, onValueChange = { onAction(ReferenceSearchAction.Edit(it)) })
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
private fun SearchHeading() {
    Text(stringResource(R.string.reference_search_title), style = DejaViewType.title)
    Text(
        text = stringResource(R.string.reference_search_heading),
        modifier = Modifier.semantics { heading() },
        style = DejaViewType.heading,
    )
    Text(
        text = stringResource(R.string.reference_search_explanation),
        style = DejaViewType.body,
        color = DejaViewColors.secondaryText,
    )
}

@Composable
private fun DescriptionField(
    state: ReferenceSearchState,
    onValueChange: (String) -> Unit,
) {
    DejaViewTextField(
        value = state.query,
        onValueChange = onValueChange,
        label = stringResource(R.string.reference_search_description),
        placeholder = stringResource(R.string.reference_search_example),
        isError = state.characterCount > MAX_QUERY_LENGTH,
        supportingText = {
            Text(
                text =
                    stringResource(
                        R.string.reference_search_count,
                        state.characterCount,
                        MAX_QUERY_LENGTH,
                        MIN_QUERY_LENGTH,
                    ),
                style = MaterialTheme.typography.labelSmall,
            )
        },
        minLines = DESCRIPTION_LINES,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        trailingIcon =
            if (state.query.isNotEmpty()) {
                {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(painterResource(R.drawable.text_search_clear), stringResource(R.string.text_search_clear))
                    }
                }
            } else {
                null
            },
    )
}

@Composable
private fun SearchFeedback(
    status: SearchStatus,
    onAction: (ReferenceSearchAction) -> Unit,
    modifier: Modifier = Modifier,
    secondaryAction: (@Composable () -> Unit)? = null,
) {
    val retry = { onAction(ReferenceSearchAction.Submit) }
    val edit = { onAction(ReferenceSearchAction.Cancel) }
    when (status) {
        SearchStatus.Editing, SearchStatus.Loading -> Unit
        SearchStatus.Empty -> ContentFeedback(FeedbackKind.Empty, onAction = edit)
        SearchStatus.Offline -> ContentFeedback(FeedbackKind.Offline, modifier, onAction = retry, secondaryAction = secondaryAction)
        SearchStatus.Failed -> ContentFeedback(FeedbackKind.Error, modifier, onAction = retry, secondaryAction = secondaryAction)
        is SearchStatus.RateLimited ->
            ContentFeedback(
                FeedbackKind.Error,
                modifier,
                title = stringResource(R.string.text_search_rate_limit),
                message =
                    status.retryAfterSeconds?.let { stringResource(R.string.text_search_retry_after, it) }
                        ?: stringResource(R.string.text_search_retry_later),
                onAction = retry,
                secondaryAction = secondaryAction,
            )
        is SearchStatus.Results ->
            status.movies.forEachIndexed { index, movie ->
                MovieCard(movie = movie, highlighted = index == 0)
            }
    }
}
