package ru.dejaview.app

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.dejaview.model.SearchMode
import ru.dejaview.navigation.SearchRoute
import ru.dejaview.search.text.reference.presentation.ReferenceSearchRoute

@Composable
internal fun SearchFallback(route: SearchRoute) {
    if (route.mode == SearchMode.TEXT && route.draftId == null) {
        ReferenceSearchRoute()
    } else {
        Text(stringResource(R.string.search_placeholder))
    }
}
