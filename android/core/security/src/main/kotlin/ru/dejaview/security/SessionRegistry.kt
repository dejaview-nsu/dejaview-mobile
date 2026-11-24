package ru.dejaview.security

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import ru.dejaview.contracts.session.SessionCookieStore
import ru.dejaview.contracts.session.SessionReader
import ru.dejaview.contracts.session.SessionRequest
import ru.dejaview.contracts.session.SessionState
import java.net.URI
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

class SessionRegistry(
    origin: String,
    private val vault: SessionVault,
    private val scope: CoroutineScope,
    private val clock: Clock = Clock.systemUTC(),
) : SessionCookieStore,
    SessionReader {
    private val loginUrl = (origin + "/api/v1/auth/login").toHttpUrlOrNull()
    private var generation = 0L
    private var active: StoredSession? = null
    private var expiryJob: Job? = null
    private val mutable = MutableStateFlow<SessionState>(SessionState.Initializing)
    override val state = mutable.asStateFlow()

    @Synchronized
    fun beginLogin(): Long {
        reset(SessionState.Guest)
        return generation
    }

    @Synchronized
    fun beginRestore(): Pair<Long, StoredSession?> {
        val saved = vault.read()
        generation++
        active = null
        expiryJob?.cancel()
        mutable.value = SessionState.Initializing
        if (saved == null || saved.origin != loginUrl?.toString()) {
            reset(SessionState.Guest)
            return generation to null
        }
        if (!saved.expiresAt.isAfter(clock.instant()) || cookie(saved) == null) {
            reset(SessionState.Expired)
            return generation to null
        }
        return generation to saved
    }

    fun candidate(
        headers: List<String>,
        sessionExpiry: Instant,
    ): StoredSession? {
        val url = loginUrl ?: return null
        val cookie = headers.mapNotNull { Cookie.parse(url, it) }.singleOrNull { it.name == "dv_session" } ?: return null
        val expires = minOf(sessionExpiry, Instant.ofEpochMilli(cookie.expiresAt))
        if (!cookie.secure || !cookie.httpOnly || cookie.value.isEmpty() || !expires.isAfter(clock.instant())) return null
        return StoredSession(cookie.toString(), url.toString(), UUID.randomUUID().toString(), expires)
    }

    fun header(
        saved: StoredSession,
        uri: URI,
    ): String? {
        val url = uri.toString().toHttpUrlOrNull() ?: return null
        val origin = loginUrl ?: return null
        if (!url.isHttps || url.host != origin.host || url.port != origin.port || saved.origin != origin.toString()) return null
        if (!saved.expiresAt.isAfter(clock.instant())) return null
        val cookie = cookie(saved)?.takeIf { it.matches(url) } ?: return null
        return cookie.name + "=" + cookie.value
    }

    @Synchronized
    fun confirm(
        token: Long,
        saved: StoredSession,
        username: String,
        avatar: String?,
        hasPassword: Boolean,
        expiresAt: Instant,
    ): Boolean {
        if (token != generation) return false
        val expires = minOf(saved.expiresAt, expiresAt)
        if (!expires.isAfter(clock.instant())) {
            reset(SessionState.Expired)
            return false
        }
        val checked = StoredSession(saved.cookie, saved.origin, saved.accountScope, expires)
        vault.write(checked)
        active = checked
        mutable.value = SessionState.Authenticated(username, avatar, hasPassword, expires, checked.accountScope)
        expiryJob?.cancel()
        expiryJob =
            scope.launch {
                delay(Duration.between(clock.instant(), expires).toMillis().coerceAtLeast(0))
                invalidate(token)
            }
        return true
    }

    @Synchronized
    fun restoreFailed(token: Long) {
        if (token == generation && mutable.value == SessionState.Initializing) mutable.value = SessionState.Guest
    }

    @Synchronized
    override fun request(uri: URI): SessionRequest {
        if (active?.expiresAt?.isAfter(clock.instant()) == false) reset(SessionState.Expired)
        return SessionRequest(generation, active?.let { header(it, uri) })
    }

    @Synchronized
    fun detach(uri: URI): SessionRequest =
        try {
            SessionRequest(generation, (active ?: vault.read())?.let { header(it, uri) })
        } finally {
            reset(SessionState.Guest)
        }

    @Synchronized
    override fun invalidate(generation: Long) {
        if (this.generation == generation) reset(SessionState.Expired)
    }

    private fun cookie(saved: StoredSession): Cookie? = loginUrl?.let { Cookie.parse(it, saved.cookie) }

    private fun reset(state: SessionState) {
        generation++
        expiryJob?.cancel()
        active = null
        mutable.value = state
        vault.clear()
    }
}
