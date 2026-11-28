package ru.dejaview.auth.data

import ru.dejaview.auth.domain.LoginResult
import ru.dejaview.contracts.AppResult
import java.time.Instant

internal data class Identity(
    val username: String,
    val avatar: String?,
    val hasPassword: Boolean,
    val expires: Instant,
)

internal sealed interface LoginReply {
    class Accepted(
        val identity: Identity,
        val cookies: List<String>,
    ) : LoginReply

    class Failure(
        val result: LoginResult.Rejected,
    ) : LoginReply
}

internal interface SessionGateway {
    suspend fun login(
        login: String,
        password: String,
        token: String?,
    ): LoginReply

    suspend fun inspect(cookie: String): AppResult<Identity>

    suspend fun logout(cookie: String): AppResult<Unit>
}
