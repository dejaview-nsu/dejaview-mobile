package ru.dejaview.search.text.reference.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ru.dejaview.search.text.reference.data.FakeReferenceSearch

private val referenceSearchFactory =
    viewModelFactory {
        initializer { ReferenceSearchViewModel(FakeReferenceSearch(), createSavedStateHandle()) }
    }

@Composable
fun ReferenceSearchRoute(modifier: Modifier = Modifier) {
    val viewModel: ReferenceSearchViewModel = viewModel(factory = referenceSearchFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReferenceSearchScreen(state = state, onAction = viewModel::onAction, modifier = modifier)
}
