package ru.dejaview.search.text.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode
import ru.dejaview.search.text.domain.MAX_QUERY_LENGTH
import ru.dejaview.search.text.domain.MIN_QUERY_LENGTH
import ru.dejaview.search.text.domain.TextSearch
import ru.dejaview.search.text.domain.boundedDraft
import ru.dejaview.search.text.domain.queryLength
import ru.dejaview.search.text.domain.trimQuery
import java.time.Instant
import java.util.UUID

private const val DRAFT_KEY = "reference-search-query"

internal data class TextSearchState(
    val query: String = "",
    val status: SearchStatus = SearchStatus.Editing,
    val retryInSeconds: Long = 0,
) {
    val characterCount: Int get() = query.trimQuery().queryLength()
    val canSubmit: Boolean
        get() = characterCount in MIN_QUERY_LENGTH..MAX_QUERY_LENGTH && status != SearchStatus.Loading && retryInSeconds == 0L
}

internal sealed interface SearchStatus {
    data object Editing : SearchStatus

    data object Loading : SearchStatus

    data object Offline : SearchStatus

    data object Failed : SearchStatus

    data class RateLimited(
        val retryAfterSeconds: Long?,
    ) : SearchStatus
}

internal sealed interface TextSearchAction {
    data class Edit(
        val query: String,
    ) : TextSearchAction

    data object Submit : TextSearchAction

    data object Cancel : TextSearchAction
}

internal class TextSearchViewModel(
    private val search: TextSearch,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val mutableState = MutableStateFlow(TextSearchState(query = savedState.get<String>(DRAFT_KEY).orEmpty().boundedDraft()))
    val state = mutableState.asStateFlow()
    private var request: Job? = null

    fun onAction(
        action: TextSearchAction,
        onCompleted: (SearchCompletion) -> Unit,
    ) {
        when (action) {
            is TextSearchAction.Edit -> edit(action.query)
            TextSearchAction.Submit -> submit(onCompleted)
            TextSearchAction.Cancel -> cancelSearch()
        }
    }

    private fun edit(text: String) {
        request?.cancel()
        val query = text.boundedDraft()
        savedState[DRAFT_KEY] = query
        mutableState.value = TextSearchState(query = query)
    }

    fun cancelSearch() {
        request?.cancel()
        mutableState.value = mutableState.value.copy(status = SearchStatus.Editing)
    }

    private fun submit(onCompleted: (SearchCompletion) -> Unit) {
        val submitted = mutableState.value
        if (!submitted.canSubmit) return
        mutableState.value = submitted.copy(status = SearchStatus.Loading)
        val requestId = UUID.randomUUID().toString()
        request =
            viewModelScope.launch {
                val query = submitted.query.trimQuery()
                val result = search.search(query)
                ensureActive()
                when (result) {
                    is AppResult.Success -> {
                        mutableState.value = submitted.copy(status = SearchStatus.Editing)
                        onCompleted(SearchCompletion(requestId, SearchMode.TEXT, result.value, query, Instant.now()))
                    }
                    is AppResult.Failure -> showFailure(submitted, result.error)
                }
            }
    }

    private suspend fun showFailure(submitted: TextSearchState, error: AppError) {
        val wait = (error as? AppError.RateLimit)?.retryAfterSeconds ?: 0
        mutableState.value = submitted.copy(status = error.toSearchStatus(), retryInSeconds = wait)
        while (mutableState.value.retryInSeconds > 0) {
            delay(1000)
            mutableState.update { it.copy(retryInSeconds = it.retryInSeconds - 1) }
        }
    }

    private fun AppError.toSearchStatus(): SearchStatus =
        when (this) {
            is AppError.Offline -> SearchStatus.Offline
            is AppError.RateLimit -> SearchStatus.RateLimited(retryAfterSeconds)
            else -> SearchStatus.Failed
        }
}
