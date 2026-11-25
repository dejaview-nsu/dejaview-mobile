package ru.dejaview.contracts.session

import kotlinx.coroutines.flow.StateFlow
import java.time.Instant

interface SessionReader {
    val state: StateFlow<SessionState>
}

sealed interface SessionState {
    data object Initializing : SessionState

    data object Guest : SessionState

    data object Expired : SessionState

    data class Authenticated(
        val username: String,
        val expiresAt: Instant,
        val accountScope: String,
    ) : SessionState
}
