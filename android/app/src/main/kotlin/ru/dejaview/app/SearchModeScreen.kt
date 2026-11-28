package ru.dejaview.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.components.SearchModeSelector
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode
import ru.dejaview.navigation.SearchRoute
import ru.dejaview.search.api.SearchModalityUi

@Composable
internal fun SearchHome(
    guest: Boolean,
    onLogin: () -> Unit,
    onProfile: () -> Unit,
    onMode: (SearchMode) -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = DejaViewSpacing.large),
        verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.app_name), style = DejaViewType.title)
            if (guest) {
                TextButton(onClick = onLogin) { Text(stringResource(R.string.search_login)) }
            } else {
                TextButton(onClick = onProfile) { Text(stringResource(R.string.profile_placeholder)) }
            }
        }
        Text(
            stringResource(R.string.search_home_heading),
            modifier = Modifier.semantics { heading() },
            style = DejaViewType.heading,
        )
        SearchModeSelector(
            selectedMode = if (guest) SearchMode.TEXT else null,
            onModeSelected = onMode,
            subtitles =
                mapOf(
                    SearchMode.TEXT to stringResource(if (guest) R.string.search_text_guest else R.string.search_text_hint),
                    SearchMode.IMAGE to stringResource(R.string.search_image_hint),
                    SearchMode.VIDEO to stringResource(R.string.search_video_hint),
                    SearchMode.AUDIO to stringResource(R.string.search_audio_hint),
                ),
        )
    }
}

@Composable
internal fun SearchModeScreen(
    route: SearchRoute,
    entrypoint: SearchModalityUi?,
    canOpen: Boolean,
    initializing: Boolean,
    onLogin: () -> Unit,
    onCompleted: (SearchCompletion) -> Unit,
) {
    when {
        canOpen && entrypoint == null -> SearchFallback(route, onCompleted, Modifier.fillMaxSize())
        canOpen -> entrypoint?.Content(route.draftId, onCompleted, Modifier.fillMaxSize())
        initializing -> Text(stringResource(R.string.search_session_loading))
        else -> PrimaryButton(stringResource(R.string.search_login_required), onLogin)
    }
}
