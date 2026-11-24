package ru.dejaview.auth.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.dejaview.auth.domain.LoginResult
import ru.dejaview.auth.domain.LoginService
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.contracts.session.SessionController
import ru.dejaview.security.SessionRegistry
import ru.dejaview.security.StoredSession
import java.io.IOException
import java.net.URI
import java.security.GeneralSecurityException

internal class SessionRepository(
    private val registry: SessionRegistry,
    private val gateway: SessionGateway,
    origin: String,
) : LoginService,
    SessionController {
    private val sessionUri = URI.create(origin + "/api/v1/auth/session")
    private val logoutUri = URI.create(origin + "/api/v1/auth/logout")

    override suspend fun login(
        login: String,
        password: String,
        captchaToken: String?,
    ): LoginResult {
        var rejection: LoginResult.Rejected? = null
        val result =
            safely {
                val generation = registry.beginLogin()
                when (val reply = gateway.login(login, password, captchaToken)) {
                    is LoginReply.Failure -> {
                        rejection = reply.result
                        AppResult.Failure(reply.result.error)
                    }
                    is LoginReply.Accepted -> {
                        val candidate =
                            registry.candidate(reply.cookies, reply.identity.expires)
                                ?: return@safely AppResult.Failure(AppError.Unknown("INVALID_SESSION"))
                        verify(generation, candidate)
                    }
                }
            }
        return if (result is AppResult.Failure) rejection ?: LoginResult.Rejected(result.error) else LoginResult.Success
    }

    override suspend fun restore() =
        safely {
            val (generation, saved) = registry.beginRestore()
            if (saved == null) return@safely AppResult.Success(Unit)
            try {
                verify(generation, saved)
            } finally {
                registry.restoreFailed(generation)
            }
        }

    private suspend fun verify(
        generation: Long,
        saved: StoredSession,
    ): AppResult<Unit> {
        val header = registry.header(saved, sessionUri) ?: return AppResult.Failure(AppError.Unknown("INVALID_SESSION"))
        return when (val session = gateway.inspect(header)) {
            is AppResult.Failure -> {
                if (session.error is AppError.Unauthenticated) registry.invalidate(generation)
                session
            }
            is AppResult.Success ->
                with(session.value) {
                    if (registry.confirm(generation, saved, username, avatar, hasPassword, expires)) {
                        AppResult.Success(Unit)
                    } else {
                        AppResult.Failure(AppError.Unauthenticated())
                    }
                }
        }
    }

    override suspend fun logout() =
        safely {
            val header = registry.detach(logoutUri).cookieHeader
            if (header == null) AppResult.Success(Unit) else gateway.logout(header)
        }

    private suspend fun safely(operation: suspend () -> AppResult<Unit>): AppResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                operation()
            } catch (_: GeneralSecurityException) {
                AppResult.Failure(AppError.Unknown("SESSION_STORAGE"))
            } catch (_: IOException) {
                AppResult.Failure(AppError.Unknown("SESSION_STORAGE"))
            }
        }
}
