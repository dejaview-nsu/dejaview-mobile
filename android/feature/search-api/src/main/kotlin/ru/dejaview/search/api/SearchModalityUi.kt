package ru.dejaview.search.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dagger.MapKey
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode

@MapKey
annotation class SearchModeKey(
    val value: SearchMode,
)

interface SearchModalityUi {
    val mode: SearchMode

    @Composable
    fun Content(
        draftId: String?,
        onCompleted: (SearchCompletion) -> Unit,
        modifier: Modifier,
    )
}
