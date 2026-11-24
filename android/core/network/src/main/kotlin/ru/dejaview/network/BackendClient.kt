package ru.dejaview.network

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.session.SessionCookieStore
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URI
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class BackendResponse internal constructor(
    val status: Int,
    val body: JsonObject?,
    val cookies: List<String>,
    val error: AppError?,
)

class BackendClient(
    private val origin: String,
    client: OkHttpClient = OkHttpClient(),
) {
    private val http =
        client
            .newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .retryOnConnectionFailure(false)
            .readTimeout(90, TimeUnit.SECONDS)
            .build()

    fun uri(path: String): URI = URI.create(origin + "/api/v1/" + path)

    suspend fun sendProtected(
        path: String,
        body: RequestBody?,
        sessions: SessionCookieStore,
    ): BackendResponse {
        val uri = uri(path)
        val session = sessions.request(uri)
        val cookie = session.cookieHeader ?: return failed(AppError.Unauthenticated())
        val reply = send(path, body, cookie)
        if (reply.status == 401) {
            sessions.invalidate(session.generation)
            return reply
        }
        if (sessions.request(uri).generation != session.generation) return failed(AppError.Unauthenticated())
        return reply
    }

    suspend fun send(
        path: String,
        body: RequestBody? = null,
        cookie: String? = null,
    ): BackendResponse {
        val url = uri(path).toString().toHttpUrlOrNull()
        if (url == null || !url.isHttps) return failed(AppError.ServiceUnavailable("API_NOT_CONFIGURED"))
        val request =
            Request
                .Builder()
                .url(url)
                .apply {
                    if (body != null) post(body)
                    if (cookie != null) header("Cookie", cookie)
                }.build()
        return suspendCancellableCoroutine { continuation ->
            val call = http.newCall(request)
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(
                object : Callback {
                    override fun onFailure(
                        call: Call,
                        e: IOException,
                    ) {
                        continuation.resume(failed(if (e is SocketTimeoutException) AppError.Timeout() else AppError.Offline()))
                    }

                    override fun onResponse(
                        call: Call,
                        response: Response,
                    ) {
                        val result =
                            try {
                                response.use {
                                    val source = it.body?.source()
                                    val json =
                                        if (source != null && !source.request(1024L * 1024)) {
                                            parseObject(source.readUtf8())
                                        } else {
                                            null
                                        }
                                    BackendResponse(it.code, json, it.headers.values("Set-Cookie"), mapError(it, json))
                                }
                            } catch (_: IOException) {
                                failed(AppError.Offline())
                            }
                        continuation.resume(result)
                    }
                },
            )
        }
    }

    private fun failed(error: AppError) = BackendResponse(0, null, emptyList(), error)

    private fun parseObject(text: String) =
        try {
            Json.parseToJsonElement(text) as? JsonObject
        } catch (_: IllegalArgumentException) {
            null
        }

    private fun mapError(
        response: Response,
        body: JsonObject?,
    ): AppError? {
        val code = (body?.get("code") as? JsonPrimitive)?.contentOrNull
        return when (response.code) {
            in 200..299 -> null
            400, 422 -> AppError.Validation((body?.get("field") as? JsonPrimitive)?.contentOrNull, code)
            401 -> AppError.Unauthenticated(code)
            403 -> AppError.Forbidden(code)
            404 -> AppError.NotFound(code)
            413, 415 -> AppError.InvalidFile(code)
            429 -> AppError.RateLimit(response.header("Retry-After")?.toLongOrNull()?.coerceAtLeast(0), code)
            503 -> AppError.ServiceUnavailable(code)
            else -> AppError.Unknown(code)
        }
    }
}
