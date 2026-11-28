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

private const val COLUMNS = 2
private val TILE_MIN_HEIGHT = 132.dp
private val TILE_ICON_SIZE = 28.dp

@Composable
fun SearchModeSelector(
    selectedMode: SearchMode?,
    onModeSelected: (SearchMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    subtitles: Map<SearchMode, String> = emptyMap(),
    accessLabel: String? = null,
) {
    Column(modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.medium)) {
        for (row in SearchMode.entries.chunked(COLUMNS)) {
            Row(horizontalArrangement = Arrangement.spacedBy(DejaViewSpacing.medium)) {
                for (mode in row) {
                    SearchModeTile(
                        mode = mode,
                        selected = selectedMode == mode,
                        onClick = { onModeSelected(mode) },
                        modifier = Modifier.weight(1f),
                        enabled = enabled,
                        subtitle = subtitles[mode],
                        accessLabel = accessLabel?.takeIf { mode != SearchMode.TEXT },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchModeTile(
    mode: SearchMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    subtitle: String?,
    accessLabel: String?,
) {
    val borderColor = if (selected) DejaViewColors.paper else DejaViewColors.border
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = DejaViewShapes.card,
        color = if (selected) DejaViewColors.paper else DejaViewColors.surface,
        contentColor = if (selected) DejaViewColors.paperText else DejaViewColors.text,
        border = BorderStroke(DejaViewShapes.outlineWidth, borderColor),
    ) {
        Column(
            modifier = Modifier.heightIn(min = TILE_MIN_HEIGHT).padding(DejaViewSpacing.large),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row {
                Icon(
                    painter = painterResource(mode.icon),
                    contentDescription = null,
                    modifier = Modifier.size(TILE_ICON_SIZE),
                    tint = if (selected) DejaViewColors.paperLink else DejaViewColors.accent,
                )
                if (accessLabel != null) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                        Text(accessLabel, color = DejaViewColors.mutedText, style = DejaViewType.caption)
                    }
                }
            }
            Column(Modifier.padding(top = DejaViewSpacing.small)) {
                Text(stringResource(mode.label), style = DejaViewType.subheading)
                if (subtitle != null) {
                    val color = if (selected) DejaViewColors.paperSecondaryText else DejaViewColors.secondaryText
                    Text(subtitle, style = DejaViewType.caption, color = color)
                }
            }
        }
    }
}

@get:StringRes
val SearchMode.label: Int
    get() =
        when (this) {
            SearchMode.TEXT -> R.string.dv_mode_text
            SearchMode.IMAGE -> R.string.dv_mode_image
            SearchMode.VIDEO -> R.string.dv_mode_video
            SearchMode.AUDIO -> R.string.dv_mode_audio
        }

@get:DrawableRes
val SearchMode.icon: Int
    get() =
        when (this) {
            SearchMode.TEXT -> R.drawable.dv_search_text
            SearchMode.IMAGE -> R.drawable.dv_search_image
            SearchMode.VIDEO -> R.drawable.dv_search_video
            SearchMode.AUDIO -> R.drawable.dv_search_audio
        }
