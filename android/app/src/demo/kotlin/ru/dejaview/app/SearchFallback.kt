package ru.dejaview.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode
import ru.dejaview.navigation.SearchRoute
import ru.dejaview.search.text.reference.presentation.ReferenceSearchRoute

@Composable
internal fun SearchFallback(
    route: SearchRoute,
    onCompleted: (SearchCompletion) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (route.mode == SearchMode.TEXT) {
        ReferenceSearchRoute(modifier, onCompleted)
    } else {
        DemoMediaSearch(route.mode, onCompleted, modifier)
    }
}
