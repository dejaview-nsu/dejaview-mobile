package ru.dejaview.audio.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.dejaview.audio.domain.AudioSearch
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.contracts.media.PreparedMedia
import ru.dejaview.contracts.session.SessionCookieStore
import ru.dejaview.contracts.session.SessionRequest
import ru.dejaview.network.BackendClient
import java.net.URI
import java.util.concurrent.TimeUnit

class AudioUploadTest {
    private val server = MockWebServer()
    private val payload = "synthetic multipart fixture".toByteArray()
    private val media = PreparedMedia("draft", "audio/mp4", payload.size.toLong(), 1000, "audio.m4a")
    private var currentGeneration = 1L
    private lateinit var adapter: AudioSearch

    @Before fun setup() {
        val cert = HeldCertificate.Builder().addSubjectAlternativeName("127.0.0.1").build()
        val tls = HandshakeCertificates.Builder().heldCertificate(cert).build()
        val trust = HandshakeCertificates.Builder().addTrustedCertificate(cert.certificate).build()
        server.useHttps(tls.sslSocketFactory(), false)
        server.start(java.net.InetAddress.getByName("127.0.0.1"), 0)
        val http =
            OkHttpClient
                .Builder()
                .proxy(
                    java.net.Proxy.NO_PROXY,
                ).sslSocketFactory(trust.sslSocketFactory(), trust.trustManager)
                .build()
        val client = BackendClient("https://127.0.0.1:" + server.port, http)
        val store =
            object : SessionCookieStore {
                override fun request(uri: URI) = SessionRequest(currentGeneration, "dv_session=sample")

                override fun invalidate(generation: Long) {
                    if (currentGeneration == generation) currentGeneration++
                }
            }
        adapter = audioSearch(client, store) { payload.inputStream() }
    }

    @After fun close() {
        server.shutdown()
    }

    @Test fun postsOneFileAndReportsPayloadBytes() =
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"results":[]}"""))
            var sent = 0L
            val response = adapter.search(media) { sent = it }
            assertTrue(response is AppResult.Success && response.value.isEmpty())
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/v1/search/audio", request.path)
            assertEquals("dv_session=sample", request.getHeader("Cookie"))
            assertEquals(media.byteCount, sent)
            val body = request.body.readUtf8()
            assertEquals(1, Regex("Content-Disposition:").findAll(body).count())
            assertTrue(body.contains("name=\"file\"; filename=\"audio.m4a\""))
            assertTrue(body.contains("Content-Type: audio/mp4"))
            assertTrue(body.contains(payload.decodeToString()))
        }

    @Test fun cancellationCanRetryAndOnly401InvalidatesSession() =
        runBlocking {
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val attempt = async(Dispatchers.IO) { adapter.search(media) {} }
            requireNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            attempt.cancelAndJoin()
            for ((status, code) in listOf(
                413 to "FILE_TOO_LARGE",
                415 to "UNSUPPORTED_FORMAT",
                422 to "AUDIO_TOO_LONG",
                503 to "SERVER_BUSY",
            )) {
                server.enqueue(MockResponse().setResponseCode(status).setBody("""{"code":"$code"}"""))
                val result = adapter.search(media) {} as AppResult.Failure
                assertEquals(code, result.error.code)
                assertEquals(1L, currentGeneration)
            }
            server.enqueue(MockResponse().setResponseCode(401))
            val result = adapter.search(media) {} as AppResult.Failure
            assertTrue(result.error is AppError.Unauthenticated)
            assertEquals(2L, currentGeneration)
        }
}
