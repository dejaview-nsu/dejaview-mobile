package ru.dejaview.security

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.dejaview.contracts.session.SessionState
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class SessionRegistryTest {
    private val origin = "https://api.example.test"
    private val audio = URI.create(origin + "/api/v1/search/audio")
    private val expiry = Instant.parse("2030-01-01T00:00:00Z")
    private val fixed = Clock.fixed(expiry.minusSeconds(10), ZoneOffset.UTC)
    private val setCookie = "dv_session=sample; Path=/api/v1; HttpOnly; Secure"
    private val vault =
        object : SessionVault {
            var saved: StoredSession? = null

            override fun read() = saved

            override fun write(session: StoredSession) {
                saved = session
            }

            override fun clear() {
                saved = null
            }
        }

    @Test fun onlyConfirmedScopedCookieSurvivesRestart() =
        runTest {
            val registry = SessionRegistry(origin, vault, backgroundScope, fixed)
            assertEquals(SessionState.Initializing, registry.state.value)
            registry.beginRestore()
            assertEquals(SessionState.Guest, registry.state.value)
            val token = registry.beginLogin()
            val candidate = requireNotNull(registry.candidate(listOf(setCookie), expiry))
            assertNull(registry.request(audio).cookieHeader)
            assertTrue(registry.confirm(token, candidate, "viewer", null, true, expiry))
            val session = registry.state.value
            assertEquals("dv_session=sample", registry.request(audio).cookieHeader)
            listOf(
                "http://api.example.test/api/v1/read",
                "https://images.example.test/api/v1/read",
                origin + "/api/v10/read",
                origin + ":4430/api/v1/read",
            ).forEach { assertNull(registry.request(URI.create(it)).cookieHeader) }

            val restarted = SessionRegistry(origin, vault, backgroundScope, fixed)
            val (next, saved) = restarted.beginRestore()
            assertEquals(SessionState.Initializing, restarted.state.value)
            assertNull(restarted.request(audio).cookieHeader)
            assertTrue(restarted.confirm(next, requireNotNull(saved), "viewer", null, true, expiry))
            assertEquals(session, restarted.state.value)
        }

    @Test fun logoutThenReauthenticationRejectsOldResponseAndOld401() =
        runTest {
            val registry = SessionRegistry(origin, vault, backgroundScope, fixed)
            val alice = registry.beginLogin()
            val candidate = requireNotNull(registry.candidate(listOf(setCookie), expiry))
            registry.confirm(alice, candidate, "first", null, true, expiry)
            assertEquals("dv_session=sample", registry.detach(audio).cookieHeader)
            assertNull(vault.saved)
            assertFalse(registry.confirm(alice, candidate, "first", null, true, expiry))
            val bob = registry.beginLogin()
            val second = requireNotNull(registry.candidate(listOf(setCookie), expiry))
            registry.confirm(bob, second, "second", null, true, expiry)
            registry.invalidate(alice)
            assertNotEquals(candidate.accountScope, second.accountScope)
            assertEquals("second", (registry.state.value as SessionState.Authenticated).username)
            registry.invalidate(bob)
            val expiredGeneration = registry.request(audio).generation
            registry.invalidate(bob)
            assertEquals(expiredGeneration, registry.request(audio).generation)
            assertEquals(SessionState.Expired, registry.state.value)
            assertNull(vault.saved)
        }

    @Test fun timerColdExpiryAndUnconfirmedRestoreKeepUserOut() =
        runTest {
            val registry = SessionRegistry(origin, vault, backgroundScope, fixed)
            val login = registry.beginLogin()
            val saved = requireNotNull(registry.candidate(listOf(setCookie), expiry))
            registry.confirm(login, saved, "user", null, true, expiry)
            val (generation, _) = registry.beginRestore()
            registry.restoreFailed(generation)
            assertNull(registry.request(audio).cookieHeader)
            assertEquals(SessionState.Guest, registry.state.value)
            val (retry, restored) = registry.beginRestore()
            registry.confirm(retry, requireNotNull(restored), "user", null, true, expiry)
            advanceTimeBy(10_000)
            runCurrent()
            assertEquals(SessionState.Expired, registry.state.value)
            vault.write(saved)
            val late = SessionRegistry(origin, vault, backgroundScope, Clock.fixed(expiry, ZoneOffset.UTC))
            assertNull(late.beginRestore().second)
            assertEquals(SessionState.Expired, late.state.value)
        }

    @Test fun refusesMissingDuplicateExpiredAndInsecureCredentials() =
        runTest {
            val registry = SessionRegistry(origin, vault, backgroundScope, fixed)
            listOf(
                emptyList(),
                listOf(setCookie, setCookie),
                listOf("other=x; Secure; HttpOnly"),
                listOf(setCookie.replace("HttpOnly", "")),
                listOf(setCookie.replace("Secure", "")),
                listOf(setCookie.replace("sample", "")),
                listOf(setCookie + "; Max-Age=0"),
            ).forEach {
                assertNull(registry.candidate(it, expiry))
            }
            assertNull(registry.candidate(listOf(setCookie), fixed.instant()))
        }
}
