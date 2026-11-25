package ru.dejaview.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.components.SearchModeSelector
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode
import ru.dejaview.navigation.SearchRoute
import ru.dejaview.search.api.SearchModalityUi

@Composable
internal fun SearchHome(onMode: (SearchMode) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = DejaViewSpacing.large),
        verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
    ) {
        Text(stringResource(R.string.search_placeholder), style = DejaViewType.heading)
        SearchModeSelector(selectedMode = null, onModeSelected = onMode)
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
