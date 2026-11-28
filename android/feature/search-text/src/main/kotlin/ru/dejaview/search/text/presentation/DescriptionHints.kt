package ru.dejaview.search.text.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.feature.searchtext.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DescriptionHints(onHint: (String) -> Unit) {
    Text(stringResource(R.string.text_search_hint_title), color = DejaViewColors.secondaryText, style = DejaViewType.bodySmall)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(DejaViewSpacing.small),
        verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.extraSmall),
    ) {
        val labels = listOf(R.string.text_search_hint_place, R.string.text_search_hint_character, R.string.text_search_hint_year, R.string.text_search_hint_scene)
        labels.forEach { label ->
            val hint = stringResource(label)
            SuggestionChip(
                onClick = { onHint(hint) },
                label = { Text(hint) },
                shape = DejaViewShapes.pill,
                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = DejaViewColors.surface),
            )
        }
    }
}
