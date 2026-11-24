package ru.dejaview.contracts.session

import kotlinx.coroutines.flow.StateFlow
import ru.dejaview.contracts.AppResult
import java.net.URI
import java.time.Instant

sealed interface SessionState {
    data object Initializing : SessionState

    data object Guest : SessionState

    data object Expired : SessionState

    data class Authenticated(
        val username: String,
        val avatarUrl: String?,
        val hasPassword: Boolean,
        val expiresAt: Instant,
        val accountScope: String,
    ) : SessionState
}

interface SessionReader {
    val state: StateFlow<SessionState>
}

interface SessionController {
    suspend fun restore(): AppResult<Unit>

    suspend fun logout(): AppResult<Unit>
}

// Keep the generation alongside the request, including when handling its response.
class SessionRequest(
    val generation: Long,
    val cookieHeader: String?,
)

interface SessionCookieStore {
    fun request(uri: URI): SessionRequest

    fun invalidate(generation: Long)
}
