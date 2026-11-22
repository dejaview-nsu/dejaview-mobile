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
import ru.dejaview.search.text.reference.domain.ReferenceSearch

internal const val MIN_QUERY_LENGTH = 3
internal const val MAX_QUERY_LENGTH = 250
private const val MAX_DRAFT_LENGTH = MAX_QUERY_LENGTH + 1
private const val DRAFT_KEY = "reference-search-query"

internal data class ReferenceSearchState(
    val query: String = "",
    val status: SearchStatus = SearchStatus.Editing,
) {
    val characterCount: Int get() = query.codePointCount(0, query.length)

    val canSubmit: Boolean
        get() {
            val trimmed = query.trim()
            return trimmed.codePointCount(0, trimmed.length) >= MIN_QUERY_LENGTH &&
                characterCount <= MAX_QUERY_LENGTH && status != SearchStatus.Loading
        }
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
    private val mutableState = MutableStateFlow(ReferenceSearchState(query = savedState.get<String>(DRAFT_KEY).orEmpty()))
    val state = mutableState.asStateFlow()
    private var request: Job? = null

    fun onAction(action: ReferenceSearchAction) {
        when (action) {
            is ReferenceSearchAction.Edit -> edit(action.query)
            ReferenceSearchAction.Submit -> submit()
            ReferenceSearchAction.Cancel -> cancelSearch()
        }
    }

    private fun edit(text: String) {
        request?.cancel()
        val query = boundedDraft(text)
        savedState[DRAFT_KEY] = query
        mutableState.value = ReferenceSearchState(query = query)
    }

    private fun cancelSearch() {
        request?.cancel()
        mutableState.value = mutableState.value.copy(status = SearchStatus.Editing)
    }

    private fun boundedDraft(query: String): String {
        // Keep one extra code point so the field can show the limit error without saving a large paste.
        if (query.codePointCount(0, query.length) <= MAX_DRAFT_LENGTH) return query
        return query.substring(0, query.offsetByCodePoints(0, MAX_DRAFT_LENGTH))
    }

    private fun submit() {
        val submitted = mutableState.value
        if (!submitted.canSubmit) return
        mutableState.value = submitted.copy(status = SearchStatus.Loading)
        request =
            viewModelScope.launch {
                val result = search.search(submitted.query.trim())
                ensureActive()
                mutableState.value = submitted.copy(status = result.toSearchStatus())
            }
    }

    private fun AppResult<List<MovieSummary>>.toSearchStatus(): SearchStatus =
        when (this) {
            is AppResult.Success -> if (value.isEmpty()) SearchStatus.Empty else SearchStatus.Results(value)
            is AppResult.Failure -> if (error is AppError.Offline) SearchStatus.Offline else SearchStatus.Failed
        }
}
