package ru.dejaview.app

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.dejaview.model.SearchCompletion
import ru.dejaview.navigation.SearchRoute

@Suppress("UNUSED_PARAMETER")
@Composable
internal fun SearchFallback(
    route: SearchRoute,
    onCompleted: (SearchCompletion) -> Unit,
    modifier: Modifier,
) {
    Text(stringResource(R.string.search_mode_unavailable), modifier = modifier)
}
