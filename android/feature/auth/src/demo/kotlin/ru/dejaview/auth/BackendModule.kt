package ru.dejaview.auth

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import ru.dejaview.auth.data.Identity
import ru.dejaview.auth.data.LoginReply
import ru.dejaview.auth.data.SessionGateway
import ru.dejaview.auth.domain.LoginResult
import ru.dejaview.auth.presentation.DemoChallenge
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.security.SessionRegistry
import ru.dejaview.security.SessionVault
import ru.dejaview.security.StoredSession
import java.time.Instant
import java.util.UUID
import javax.inject.Singleton

internal val backendOrigin: String get() = "https://demo.invalid"

@Module
@InstallIn(SingletonComponent::class)
internal object BackendModule {
    @Provides @Singleton
    fun gateway(): SessionGateway = DemoGateway()

    @Provides @Singleton
    fun sessions(): SessionRegistry {
        val memory =
            object : SessionVault {
                private var session: StoredSession? = null

                override fun read() = session

                override fun write(session: StoredSession) {
                    this.session = session
                }

                override fun clear() {
                    session = null
                }
            }
        return SessionRegistry(backendOrigin, memory, CoroutineScope(SupervisorJob() + Dispatchers.IO))
    }
}

internal class DemoGateway : SessionGateway {
    private val users = java.util.concurrent.ConcurrentHashMap<String, Identity>()

    override suspend fun login(
        login: String,
        password: String,
        token: String?,
    ): LoginReply {
        delay(250)
        val error =
            when (login) {
                "invalid" -> AppError.Validation("login", "AUTH_INVALID_CREDENTIALS")
                "unconfirmed" -> AppError.Forbidden("AUTH_EMAIL_NOT_CONFIRMED")
                "blocked" -> AppError.Forbidden("AUTH_ACCOUNT_BLOCKED")
                "captcha" ->
                    when {
                        token == null -> AppError.Forbidden("AUTH_CAPTCHA_REQUIRED")
                        DemoChallenge.consume(token) -> null
                        else -> AppError.Forbidden("AUTH_CAPTCHA_INVALID")
                    }
                "locked" -> AppError.RateLimit(4, "AUTH_LOGIN_LOCKED")
                "offline" -> AppError.Offline()
                "error" -> AppError.Unknown()
                else -> null
            }
        if (error != null) return LoginReply.Failure(LoginResult.Rejected(error, login == "captcha"))
        val cookie = "dv_session=" + UUID.randomUUID()
        val identity = Identity(login, null, true, Instant.now().plusSeconds(3600))
        users[cookie] = identity
        return LoginReply.Accepted(identity, listOf(cookie + "; Secure; HttpOnly; Path=/api/v1; Max-Age=3600"))
    }

    override suspend fun inspect(cookie: String): AppResult<Identity> =
        users[cookie]?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.Unauthenticated())

    override suspend fun logout(cookie: String): AppResult<Unit> {
        users.remove(cookie)
        return AppResult.Success(Unit)
    }
}
