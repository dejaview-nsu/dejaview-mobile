package ru.dejaview.search.text.reference.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.model.MovieSummary
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode
import ru.dejaview.search.text.reference.domain.ReferenceSearch
import ru.dejaview.search.text.reference.domain.MAX_QUERY_LENGTH
import ru.dejaview.search.text.reference.domain.MIN_QUERY_LENGTH
import ru.dejaview.search.text.reference.domain.boundedDraft
import ru.dejaview.search.text.reference.domain.queryLength
import ru.dejaview.search.text.reference.domain.trimQuery
import java.time.Instant
import java.util.UUID

private const val DRAFT_KEY = "reference-search-query"

internal data class ReferenceSearchState(
    val query: String = "",
    val status: SearchStatus = SearchStatus.Editing,
) {
    val characterCount: Int get() = query.trimQuery().queryLength()
    val canSubmit: Boolean get() = characterCount in MIN_QUERY_LENGTH..MAX_QUERY_LENGTH && status != SearchStatus.Loading
}

internal sealed interface SearchStatus {
    data object Editing : SearchStatus

    data object Loading : SearchStatus

    data object Empty : SearchStatus

    data object Offline : SearchStatus

    data object Failed : SearchStatus

    data class Results(
        val movies: List<MovieSummary>,
    ) : SearchStatus
}

internal sealed interface ReferenceSearchAction {
    data class Edit(
        val query: String,
    ) : ReferenceSearchAction

    data object Submit : ReferenceSearchAction

    data object Cancel : ReferenceSearchAction
}

internal class ReferenceSearchViewModel(
    private val search: ReferenceSearch,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ReferenceSearchState(query = savedState.get<String>(DRAFT_KEY).orEmpty().boundedDraft()))
    val state = mutableState.asStateFlow()
    private var request: Job? = null

    fun onAction(
        action: ReferenceSearchAction,
        onCompleted: ((SearchCompletion) -> Unit)? = null,
    ) {
        when (action) {
            is ReferenceSearchAction.Edit -> edit(action.query)
            ReferenceSearchAction.Submit -> submit(onCompleted)
            ReferenceSearchAction.Cancel -> cancelSearch()
        }
    }

    private fun edit(text: String) {
        request?.cancel()
        val query = text.boundedDraft()
        savedState[DRAFT_KEY] = query
        mutableState.value = ReferenceSearchState(query = query)
    }

    private fun cancelSearch() {
        request?.cancel()
        mutableState.value = mutableState.value.copy(status = SearchStatus.Editing)
    }

    private fun submit(onCompleted: ((SearchCompletion) -> Unit)?) {
        val submitted = mutableState.value
        if (!submitted.canSubmit) return
        mutableState.value = submitted.copy(status = SearchStatus.Loading)
        request =
            viewModelScope.launch {
                val query = submitted.query.trimQuery()
                val result = search.search(query)
                ensureActive()
                if (result is AppResult.Success && onCompleted != null) {
                    mutableState.value = submitted
                    onCompleted(SearchCompletion(UUID.randomUUID().toString(), SearchMode.TEXT, result.value, query, Instant.now()))
                } else {
                    mutableState.value = submitted.copy(status = result.toSearchStatus())
                }
            }
    }

    private fun AppResult<List<MovieSummary>>.toSearchStatus(): SearchStatus =
        when (this) {
            is AppResult.Success -> if (value.isEmpty()) SearchStatus.Empty else SearchStatus.Results(value)
            is AppResult.Failure -> if (error is AppError.Offline) SearchStatus.Offline else SearchStatus.Failed
        }
}
