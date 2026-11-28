package ru.dejaview.auth.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.dejaview.auth.domain.LoginResult
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.network.BackendClient
import ru.dejaview.security.SessionRegistry
import ru.dejaview.security.SessionVault
import ru.dejaview.security.StoredSession
import java.time.Instant

class HttpSessionTest {
    private val server = MockWebServer()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var sessions: SessionRegistry
    private lateinit var repository: SessionRepository
    private lateinit var gateway: HttpSessionGateway
    private lateinit var registration: HttpRegistrationService
    private var persisted: StoredSession? = null
    private val identity = """{"user":{"username":"viewer","avatar_url":null,"has_password":true},"expires_at":"${Instant.now().plusSeconds(
        600,
    )}"}"""

    @Before fun setup() {
        val cert = HeldCertificate.Builder().addSubjectAlternativeName("127.0.0.1").build()
        val serverTls = HandshakeCertificates.Builder().heldCertificate(cert).build()
        val trust = HandshakeCertificates.Builder().addTrustedCertificate(cert.certificate).build()
        server.useHttps(serverTls.sslSocketFactory(), false)
        server.start(java.net.InetAddress.getByName("127.0.0.1"), 0)
        val origin = "https://127.0.0.1:" + server.port
        val vault =
            object : SessionVault {
                override fun read() = persisted

                override fun write(session: StoredSession) {
                    persisted = session
                }

                override fun clear() {
                    persisted = null
                }
            }
        sessions = SessionRegistry(origin, vault, scope)
        val http =
            OkHttpClient
                .Builder()
                .proxy(
                    java.net.Proxy.NO_PROXY,
                ).sslSocketFactory(trust.sslSocketFactory(), trust.trustManager)
                .build()
        val client = BackendClient(origin, http)
        gateway = HttpSessionGateway(client)
        registration = HttpRegistrationService(client)
        repository = SessionRepository(sessions, gateway, origin)
    }

    @After fun tearDown() {
        scope.cancel()
        server.shutdown()
    }

    @Test fun registrationAndResendUseThePublicEmailContract() =
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(201).setBody("""{"resend_after":60}"""))
            assertEquals(AppResult.Success(60), registration.register("viewer", "viewer@example.test", 1998, "Cinema!2000"))
            val register = server.takeRequest()
            assertEquals("/api/v1/auth/register", register.path)
            assertEquals("POST", register.method)
            assertNull(register.getHeader("Cookie"))
            assertEquals(
                """{"username":"viewer","email":"viewer@example.test","birth_year":1998,"password":"Cinema!2000"}""",
                register.body.readUtf8(),
            )

            server.enqueue(MockResponse().setResponseCode(202).setBody("""{"resend_after":30}"""))
            assertEquals(AppResult.Success(30), registration.resend("viewer@example.test"))
            val resend = server.takeRequest()
            assertEquals("/api/v1/auth/resend-confirmation", resend.path)
            assertEquals("""{"login":"viewer@example.test"}""", resend.body.readUtf8())
            assertEquals(SessionState.Initializing, sessions.state.value)
        }

    @Test fun rejectsMissingConfirmationDelayAndPreservesRateLimit() =
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(202).setBody("{}"))
            assertEquals(AppResult.Failure(AppError.Unknown("INVALID_RESPONSE")), registration.resend("viewer@example.test"))
            server.enqueue(MockResponse().setResponseCode(429).addHeader("Retry-After", "17"))
            assertEquals(AppResult.Failure(AppError.RateLimit(17)), registration.resend("viewer@example.test"))
        }

    @Test fun verifiesCookieBeforePublishingAndDetachesBeforeLogoutFailure() =
        runBlocking {
            server.enqueue(MockResponse().setBody(identity).addHeader("Set-Cookie", "dv_session=sample; Secure; HttpOnly; Path=/api/v1"))
            server.enqueue(MockResponse().setBody(identity))
            assertEquals(LoginResult.Success, repository.login("viewer", "x", null))
            val login = server.takeRequest()
            assertEquals("/api/v1/auth/login", login.path)
            assertEquals("POST", login.method)
            assertEquals("""{"login":"viewer","password":"x"}""", login.body.readUtf8())
            val inspect = server.takeRequest()
            assertEquals("GET", inspect.method)
            assertEquals("/api/v1/auth/session", inspect.path)
            assertEquals("dv_session=sample", inspect.getHeader("Cookie"))
            assertNotNull(persisted)
            val namespace = (sessions.state.value as SessionState.Authenticated).accountScope
            server.enqueue(MockResponse().setResponseCode(503))
            assertTrue(repository.restore() is AppResult.Failure)
            assertEquals(SessionState.Guest, sessions.state.value)
            server.enqueue(MockResponse().setBody(identity))
            assertTrue(repository.restore() is AppResult.Success)
            assertEquals(namespace, (sessions.state.value as SessionState.Authenticated).accountScope)
            server.enqueue(MockResponse().setResponseCode(503))
            assertTrue(repository.logout() is AppResult.Failure)
            assertNull(persisted)
            assertEquals(SessionState.Guest, sessions.state.value)
        }

    @Test fun refusesBodyWithoutCookieAndCookieWithoutConfirmedIdentity() =
        runBlocking {
            server.enqueue(MockResponse().setBody(identity))
            assertTrue(repository.login("viewer", "x", null) is LoginResult.Rejected)
            server.enqueue(MockResponse().setBody(identity).addHeader("Set-Cookie", "dv_session=sample; Secure; HttpOnly; Path=/api/v1"))
            server.enqueue(MockResponse().setResponseCode(401))
            assertTrue(repository.login("viewer", "x", null) is LoginResult.Rejected)
            assertNull(persisted)
            assertFalse(sessions.state.value is SessionState.Authenticated)
        }

    @Test fun challengeAndLockUseServerFieldsAndDoNotExpireTheSession() =
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(400).setBody("""{"code":"AUTH_INVALID_CREDENTIALS","captcha_required":true}"""))
            val rejection = repository.login("viewer", "x", null) as LoginResult.Rejected
            assertTrue(rejection.requiresCaptcha)
            assertEquals("AUTH_INVALID_CREDENTIALS", rejection.error.code)
            server.takeRequest()
            server.enqueue(MockResponse().setResponseCode(403).setBody("""{"code":"AUTH_CAPTCHA_INVALID"}"""))
            assertTrue((repository.login("viewer", "x", "sample") as LoginResult.Rejected).requiresCaptcha)
            assertTrue(
                server
                    .takeRequest()
                    .body
                    .readUtf8()
                    .contains("captcha_token"),
            )
            server.enqueue(MockResponse().setResponseCode(429).addHeader("Retry-After", "17").setBody("""{"code":"AUTH_LOGIN_LOCKED"}"""))
            val locked = repository.login("viewer", "x", null) as LoginResult.Rejected
            assertEquals(17L, (locked.error as AppError.RateLimit).retryAfterSeconds)
            assertEquals(SessionState.Guest, sessions.state.value)
        }

    @Test fun logoutAcceptsEmpty204AndAlreadyExpired401() =
        runBlocking {
            for (status in listOf(204, 401)) {
                server.enqueue(MockResponse().setResponseCode(status))
                assertTrue(gateway.logout("dv_session=sample") is AppResult.Success)
            }
            server.enqueue(MockResponse().setBody("<html>oops</html>"))
            assertTrue(gateway.inspect("dv_session=sample") is AppResult.Failure)
        }
}
