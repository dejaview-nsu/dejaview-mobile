package ru.dejaview.security

import java.time.Instant

class StoredSession(
    val cookie: String,
    val origin: String,
    val accountScope: String,
    val expiresAt: Instant,
)

interface SessionVault {
    fun read(): StoredSession?

    fun write(session: StoredSession)

    fun clear()
}
