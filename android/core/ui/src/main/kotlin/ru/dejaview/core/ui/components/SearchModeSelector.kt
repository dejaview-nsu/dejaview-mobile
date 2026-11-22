package ru.dejaview.core.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.R
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.model.SearchMode

@Composable
fun SearchModeSelector(
    selectedMode: SearchMode,
    onModeSelected: (SearchMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    subtitles: Map<SearchMode, String> = emptyMap(),
) {
    Column(modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.medium)) {
        for (row in SearchMode.entries.chunked(2)) {
            Row(horizontalArrangement = Arrangement.spacedBy(DejaViewSpacing.medium)) {
                for (mode in row) {
                    val selected = selectedMode == mode
                    Surface(
                        selected = selected,
                        onClick = { onModeSelected(mode) },
                        modifier = Modifier.weight(1f),
                        enabled = enabled,
                        shape = DejaViewShapes.card,
                        color = if (selected) DejaViewColors.paper else DejaViewColors.surface,
                        contentColor = if (selected) DejaViewColors.paperText else DejaViewColors.text,
                        border = BorderStroke(1.dp, if (selected) DejaViewColors.paper else DejaViewColors.border),
                    ) {
                        Column(
                            modifier = Modifier.heightIn(min = 132.dp).padding(DejaViewSpacing.large),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Icon(
                                painter = painterResource(mode.icon),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = if (selected) DejaViewColors.paperLink else DejaViewColors.accent,
                            )
                            Column(Modifier.padding(top = DejaViewSpacing.small)) {
                                Text(stringResource(mode.label), style = DejaViewType.subheading)
                                subtitles[mode]?.let {
                                    Text(
                                        text = it,
                                        style = DejaViewType.caption,
                                        color = if (selected) DejaViewColors.paperSecondaryText else DejaViewColors.secondaryText,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@get:StringRes
private val SearchMode.label: Int
    get() =
        when (this) {
            SearchMode.TEXT -> R.string.dv_mode_text
            SearchMode.IMAGE -> R.string.dv_mode_image
            SearchMode.VIDEO -> R.string.dv_mode_video
            SearchMode.AUDIO -> R.string.dv_mode_audio
        }

@get:DrawableRes
private val SearchMode.icon: Int
    get() =
        when (this) {
            SearchMode.TEXT -> R.drawable.dv_search_text
            SearchMode.IMAGE -> R.drawable.dv_search_image
            SearchMode.VIDEO -> R.drawable.dv_search_video
            SearchMode.AUDIO -> R.drawable.dv_search_audio
        }
