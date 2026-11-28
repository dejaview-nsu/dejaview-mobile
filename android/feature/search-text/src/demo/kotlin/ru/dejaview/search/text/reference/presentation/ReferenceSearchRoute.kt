package ru.dejaview.search.text.reference.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ru.dejaview.model.SearchCompletion
import ru.dejaview.search.text.reference.data.FakeReferenceSearch

private val referenceSearchFactory =
    viewModelFactory {
        initializer { ReferenceSearchViewModel(FakeReferenceSearch(), createSavedStateHandle()) }
    }

@Composable
fun ReferenceSearchRoute(
    modifier: Modifier = Modifier,
    onCompleted: ((SearchCompletion) -> Unit)? = null,
) {
    val viewModel: ReferenceSearchViewModel = viewModel(factory = referenceSearchFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val callback by rememberUpdatedState(onCompleted)
    DisposableEffect(viewModel) {
        onDispose { viewModel.onAction(ReferenceSearchAction.Cancel) }
    }
    ReferenceSearchScreen(
        state = state,
        onAction = { action ->
            viewModel.onAction(action, callback)
        },
        modifier = modifier,
    )
}
