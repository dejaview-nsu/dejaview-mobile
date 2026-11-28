package ru.dejaview.search.text.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.model.MovieSummary
import ru.dejaview.network.BackendClient
import java.net.InetAddress
import java.net.Proxy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class HttpTextSearchTest {
    private val server = MockWebServer()
    private val cancelled = CountDownLatch(1)
    private lateinit var search: HttpTextSearch

    @Before
    fun startServer() {
        val certificate = HeldCertificate.Builder().addSubjectAlternativeName("127.0.0.1").build()
        val tls = HandshakeCertificates.Builder().heldCertificate(certificate).build()
        val trust = HandshakeCertificates.Builder().addTrustedCertificate(certificate.certificate).build()
        server.useHttps(tls.sslSocketFactory(), false)
        server.start(InetAddress.getByName("127.0.0.1"), 0)
        val http =
            OkHttpClient
                .Builder()
                .proxy(Proxy.NO_PROXY)
                .eventListener(
                    object : EventListener() {
                        override fun canceled(call: Call) = cancelled.countDown()
                    },
                ).sslSocketFactory(trust.sslSocketFactory(), trust.trustManager)
                .build()
        search = HttpTextSearch(BackendClient("https://127.0.0.1:${server.port}", http))
    }

    @After
    fun closeServer() = server.shutdown()

    @Test
    fun sendsGuestJsonAndKeepsServerOrderAndNulls() =
        runBlocking {
            val query = "Поезд 🎬"
            respond("""{"results":[$FULL_CARD,$NULL_CARD],"extra":42}""")
            val expected = listOf(MovieSummary(42, "Пример", 2024, "https://example.com/p.png", "Описание"), MovieSummary(4, "Пусто"))
            assertEquals(AppResult.Success(expected), search.search(query))

            val request = checkNotNull(server.takeRequest(3, TimeUnit.SECONDS))
            assertEquals("POST", request.method)
            assertEquals("/api/v1/search/text", request.path)
            assertNull(request.getHeader("Cookie"))
            assertEquals("application/json", request.getHeader("Content-Type")?.substringBefore(';'))
            assertEquals(buildJsonObject { put("query", query) }, Json.parseToJsonElement(request.body.readUtf8()))
        }

    @Test
    fun distinguishesEmptyFromBrokenSchema() =
        runBlocking {
            respond("""{"results":[]}""")
            assertEquals(AppResult.Success(emptyList<MovieSummary>()), search.search("abc"))
            val broken =
                listOf(
                    "{}",
                    """{"results":[null]}""",
                    """{"results":[{"title":"Пусто","year":null,"poster_url":null,"overview":null}]}""",
                    """{"results":[{"movie_id":4,"year":null,"poster_url":null,"overview":null}]}""",
                    """{"results":[{"movie_id":4,"title":"Пусто","poster_url":null,"overview":null}]}""",
                    """{"results":[{"movie_id":4,"title":"Пусто","year":null,"overview":null}]}""",
                    """{"results":[{"movie_id":4,"title":"Пусто","year":null,"poster_url":null}]}""",
                    """{"results":[${NULL_CARD.replace("\"movie_id\":4", "\"movie_id\":\"4\"")}]}""",
                    """{"results":[${NULL_CARD.replace("\"year\":null", "\"year\":\"2024\"")}]}""",
                    """{"results":[${NULL_CARD.replace("\"year\":null", "\"year\":true")}]}""",
                    """{"results":[${NULL_CARD.replace("\"poster_url\":null", "\"poster_url\":7")}]}""",
                    """{"results":[${NULL_CARD.replace("\"overview\":null", "\"overview\":7")}]}""",
                    """{"results":[${List(11) { NULL_CARD }.joinToString(",")}]}""",
                )
            broken.forEach { body ->
                respond(body)
                assertEquals(AppResult.Failure(AppError.Unknown("INVALID_RESPONSE")), search.search("abc"))
            }

            server.enqueue(MockResponse().setResponseCode(204))
            assertEquals(AppResult.Failure(AppError.Unknown("INVALID_RESPONSE")), search.search("abc"))
        }

    @Test
    fun keepsOfflineServiceUnavailableAndRateLimitDistinct() =
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(503).setBody("""{"code":"SEARCH_UNAVAILABLE"}"""))
            assertEquals(AppResult.Failure(AppError.ServiceUnavailable("SEARCH_UNAVAILABLE")), search.search("abc"))

            server.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", "12").setBody("<html>Rate limit</html>"))
            assertEquals(AppResult.Failure(AppError.RateLimit(12)), search.search("abc"))

            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
            assertEquals(AppResult.Failure(AppError.Offline()), search.search("abc"))
        }

    @Test
    fun cancelsTheInFlightHttpCall() =
        runBlocking {
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val attempt = async(Dispatchers.Default) { search.search("abc") }
            try {
                checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            } finally {
                attempt.cancelAndJoin()
            }
            assertTrue(cancelled.await(3, TimeUnit.SECONDS))
        }

    private fun respond(json: String) {
        server.enqueue(MockResponse().setBody(json))
    }

    private companion object {
        const val FULL_CARD =
            """{"movie_id":42,"title":"Пример","year":2024,""" +
                """"poster_url":"https://example.com/p.png","overview":"Описание","extra":true}"""
        const val NULL_CARD = """{"movie_id":4,"title":"Пусто","year":null,"poster_url":null,"overview":null}"""
    }
}
