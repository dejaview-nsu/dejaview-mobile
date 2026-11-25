package ru.dejaview.app

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import ru.dejaview.model.SearchCompletion
import ru.dejaview.navigation.SearchRoute

@Composable
internal fun SearchFallback(
    @Suppress("UNUSED_PARAMETER") route: SearchRoute,
) {
    Text(stringResource(R.string.search_placeholder), modifier = modifier)
}
