package ru.dejaview.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.dejaview.contracts.history.HistoryRepository
import ru.dejaview.contracts.session.SessionReader
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.model.RequestId
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode
import ru.dejaview.navigation.SearchRoute

class SearchViewModel(
    sessionReader: SessionReader,
    private val history: HistoryRepository,
) : ViewModel() {
    val session = sessionReader.state
    private val pendingActions = PendingActionStore()
    private val results = SearchResultStore()
    private var resultScope = session.value.accountScope

    fun canOpen(mode: SearchMode): Boolean = mode == SearchMode.TEXT || session.value is SessionState.Authenticated

    fun requestLogin(route: SearchRoute): String? = if (canOpen(route.mode)) null else pendingActions.put(route)

    fun finishLogin(actionId: String?): SearchRoute? = pendingActions.take(actionId)?.takeIf { canOpen(it.mode) }

    fun cancelLogin(actionId: String?) {
        pendingActions.take(actionId)
    }

    fun complete(
        completion: SearchCompletion,
        sourceMode: SearchMode,
        sourceSession: SessionState,
    ): Boolean {
        val currentSession = session.value
        val scope = currentSession.accountScope
        if (completion.mode != sourceMode || !canOpen(sourceMode)) return false
        if (sourceSession.accountScope != scope) return false
        updateResultScope(scope)
        if (!results.add(completion)) return false

        viewModelScope.launch { history.record(completion, scope) }
        return true
    }

    suspend fun result(requestId: RequestId): SearchCompletion? {
        val initialized = session.first { it != SessionState.Initializing }
        val scope = initialized.accountScope
        updateResultScope(scope)
        val completion =
            results[requestId]
                ?: history.observe(scope).first().firstOrNull { it.requestId == requestId }
        return completion.takeIf { session.value.accountScope == scope }
    }

    private fun updateResultScope(scope: String?) {
        if (scope != resultScope) {
            results.clear()
            resultScope = scope
        }
    }
}

private val SessionState.accountScope: String?
    get() = (this as? SessionState.Authenticated)?.accountScope
