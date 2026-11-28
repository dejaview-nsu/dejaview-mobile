package ru.dejaview.search.text.reference.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode
import ru.dejaview.search.api.SearchModalityUi
import ru.dejaview.search.api.SearchModeKey
import ru.dejaview.search.text.reference.domain.ReferenceSearch
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
internal interface TextSearchModule {
    @Binds @IntoMap
    @SearchModeKey(SearchMode.TEXT)
    fun mode(text: TextSearchMode): SearchModalityUi
}

internal class TextSearchMode
    @Inject
    constructor(
        private val search: ReferenceSearch,
    ) : SearchModalityUi {
        override val mode = SearchMode.TEXT

        @Composable
        override fun Content(
            draftId: String?,
            onCompleted: (SearchCompletion) -> Unit,
            modifier: Modifier,
        ) {
            val model = viewModel { ReferenceSearchViewModel(search, createSavedStateHandle()) }
            val state by model.state.collectAsStateWithLifecycle()
            val callback by rememberUpdatedState(onCompleted)
            val cancel = { model.onAction(ReferenceSearchAction.Cancel) }
            BackHandler(enabled = state.status == SearchStatus.Loading, onBack = cancel)
            LifecycleResumeEffect(model) {
                onPauseOrDispose {
                    if (model.state.value.status == SearchStatus.Loading) cancel()
                }
            }
            ReferenceSearchScreen(state, { action -> model.onAction(action) { callback(it) } }, modifier)
        }
    }
