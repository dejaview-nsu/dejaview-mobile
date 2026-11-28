package ru.dejaview.contracts.session

import ru.dejaview.contracts.AppResult
import java.net.URI

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
