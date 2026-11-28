package ru.dejaview.search.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.dejaview.core.ui.components.FeedbackKind
import ru.dejaview.core.ui.components.SearchStatusPage
import ru.dejaview.core.ui.components.SecondaryButton
import ru.dejaview.feature.search.R
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode

@Composable
internal fun EmptySearchResults(completion: SearchCompletion, onEdit: () -> Unit, onChooseMode: () -> Unit) {
    val isText = completion.mode == SearchMode.TEXT
    SearchStatusPage(
        FeedbackKind.Empty,
        stringResource(if (isText) R.string.search_text_header else R.string.search_results_header),
        onPrimary = onEdit,
        onSecondary = onChooseMode,
        onBack = onEdit,
        primary = stringResource(if (isText) R.string.search_refine_description else R.string.search_return),
        secondary = stringResource(if (isText) R.string.search_other_mode else R.string.search_home),
        intro = if (isText) ({ QuerySummary(completion.inputSummary, onEdit) }) else null,
        details =
            if (isText) {
                {
                    listOf(R.string.search_prompt_place, R.string.search_prompt_person, R.string.search_prompt_scene).forEach { prompt ->
                        SecondaryButton(stringResource(prompt), onEdit, Modifier.fillMaxWidth())
                    }
                }
            } else {
                null
            },
    )
}
