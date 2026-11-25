package ru.dejaview.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ru.dejaview.contracts.AppResult
import ru.dejaview.contracts.history.HistoryRepository
import ru.dejaview.contracts.session.SessionReader
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.model.SearchCompletion
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SearchDefaults
    @Inject
    constructor() : SessionReader,
        HistoryRepository {
        private val mutableState = MutableStateFlow<SessionState>(SessionState.Guest)
        override val state = mutableState.asStateFlow()
        private val entries = MutableStateFlow<Map<String?, List<SearchCompletion>>>(emptyMap())

        val demoLogin: () -> Unit = {
            mutableState.value = SessionState.Authenticated("Демо", Instant.MAX, UUID.randomUUID().toString())
        }

        fun expire() {
            val scope = (state.value as? SessionState.Authenticated)?.accountScope ?: return
            mutableState.value = SessionState.Expired
            entries.update { it - scope }
        }

        override suspend fun record(
            completion: SearchCompletion,
            accountScope: String?,
        ): AppResult<Unit> {
            if (accountScope != null && (state.value as? SessionState.Authenticated)?.accountScope != accountScope) {
                return AppResult.Failure(ru.dejaview.contracts.AppError.Unauthenticated())
            }
            entries.update { current ->
                val previous = current[accountScope].orEmpty()
                if (previous.any { it.requestId == completion.requestId }) current
                else current + (accountScope to (listOf(completion) + previous))
            }
            return AppResult.Success(Unit)
        }

        override fun observe(accountScope: String?) = entries.map { it[accountScope].orEmpty() }

        override suspend fun clear(accountScope: String?): AppResult<Unit> {
            entries.update { it - accountScope }
            return AppResult.Success(Unit)
        }
    }
