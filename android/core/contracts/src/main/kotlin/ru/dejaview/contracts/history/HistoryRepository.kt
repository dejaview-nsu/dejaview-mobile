package ru.dejaview.contracts.history

import kotlinx.coroutines.flow.Flow
import ru.dejaview.contracts.AppResult
import ru.dejaview.model.SearchCompletion

interface HistoryRepository {
    /** A null account scope identifies the temporary guest history. */
    suspend fun record(
        completion: SearchCompletion,
        accountScope: String?,
    ): AppResult<Unit>

    fun observe(accountScope: String?): Flow<List<SearchCompletion>>

    suspend fun clear(accountScope: String?): AppResult<Unit>
}
