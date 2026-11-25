package ru.dejaview.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.contracts.history.HistoryRepository
import ru.dejaview.contracts.session.SessionReader
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.model.SearchCompletion
import javax.inject.Inject

internal class SearchDefaults
    @Inject
    constructor() :
    SessionReader,
        HistoryRepository {
        override val state = MutableStateFlow<SessionState>(SessionState.Guest).asStateFlow()
        val demoLogin: (() -> Unit)? = null

        fun expire() = Unit

        override suspend fun record(
            completion: SearchCompletion,
            accountScope: String?,
        ) = AppResult.Failure(AppError.ServiceUnavailable())

        override fun observe(accountScope: String?) = flowOf(emptyList<SearchCompletion>())

        override suspend fun clear(accountScope: String?) = AppResult.Failure(AppError.ServiceUnavailable())
    }
