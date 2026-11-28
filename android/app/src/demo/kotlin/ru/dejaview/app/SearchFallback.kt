package ru.dejaview.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.dejaview.model.SearchCompletion
import ru.dejaview.navigation.SearchRoute

@Composable
internal fun SearchFallback(
    route: SearchRoute,
    onCompleted: (SearchCompletion) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    DemoMediaSearch(route.mode, onCompleted, modifier)
}
