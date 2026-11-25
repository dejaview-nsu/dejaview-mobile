package ru.dejaview.search.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.contracts.history.HistoryRepository
import ru.dejaview.contracts.session.SessionReader
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.model.MovieSummary
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode
import java.time.Instant

internal class TestSession(initial: SessionState = SessionState.Guest) : SessionReader {
    override val state = MutableStateFlow(initial)
}

internal class TestHistory(private val fails: Boolean = false) : HistoryRepository {
    val entries = mutableMapOf<String?, MutableStateFlow<List<SearchCompletion>>>()
    val writes = mutableListOf<Pair<String?, SearchCompletion>>()

    override suspend fun record(completion: SearchCompletion, accountScope: String?): AppResult<Unit> {
        writes += accountScope to completion
        if (fails) return AppResult.Failure(AppError.ServiceUnavailable())
        val entry = observe(accountScope)
        if (entry.value.none { it.requestId == completion.requestId }) entry.value += completion
        return AppResult.Success(Unit)
    }

    override fun observe(accountScope: String?) = entries.getOrPut(accountScope) { MutableStateFlow(emptyList()) }

    override suspend fun clear(accountScope: String?): AppResult<Unit> {
        observe(accountScope).value = emptyList()
        return AppResult.Success(Unit)
    }
}

internal fun syntheticCompletion(
    requestId: String = "search-1",
    mode: SearchMode = SearchMode.TEXT,
    count: Int = 1,
) = SearchCompletion(
    requestId,
    mode,
    List(count) { index -> MovieSummary((index + 1).toLong(), "Фильм ${index + 1}") },
    "Синтетический запрос",
    Instant.EPOCH,
)

internal fun signedIn(scope: String = "scope-1") = SessionState.Authenticated("Демо", Instant.MAX, scope)
