package ru.dejaview.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.R
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewType

@Composable
fun SearchLoadingScreen(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.dv_loading),
    progress: Float? = null,
    detail: String? = null,
    header: String? = null,
) {
    ScreenLayout(
        modifier,
        header = header?.let { { ScreenHeader(it, onCancel, close = true) } },
        actions = {
            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.dv_cancel)) }
        },
    ) {
        SearchArtwork(Modifier.fillMaxWidth().height(190.dp))
        Text(
            title,
            Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
            style = DejaViewType.heading,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.dv_loading_message),
            Modifier.fillMaxWidth(),
            style = DejaViewType.body,
            color = DejaViewColors.secondaryText,
            textAlign = TextAlign.Center,
        )
        if (detail != null) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(detail, style = DejaViewType.bodyMedium)
                if (progress != null) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
