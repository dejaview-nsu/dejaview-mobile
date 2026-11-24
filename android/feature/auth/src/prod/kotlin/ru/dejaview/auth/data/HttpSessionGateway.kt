package ru.dejaview.auth.data

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.dejaview.auth.domain.LoginResult
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.network.BackendClient
import java.time.Instant
import java.time.format.DateTimeParseException

internal class HttpSessionGateway(
    private val client: BackendClient,
) : SessionGateway {
    override suspend fun login(
        login: String,
        password: String,
        token: String?,
    ): LoginReply {
        val payload =
            buildJsonObject {
                put("login", login)
                put("password", password)
                token?.let { put("captcha_token", it) }
            }.toString().toRequestBody("application/json".toMediaType())
        val reply = client.send("auth/login", payload)
        val identity = identity(reply.body)
        return if (reply.status == 200 && identity != null) {
            LoginReply.Accepted(identity, reply.cookies)
        } else {
            LoginReply.Failure(
                LoginResult.Rejected(
                    reply.error ?: AppError.Unknown(),
                    (reply.body?.get("captcha_required") as? JsonPrimitive)?.booleanOrNull == true ||
                        reply.error?.code in setOf("AUTH_CAPTCHA_REQUIRED", "AUTH_CAPTCHA_INVALID"),
                ),
            )
        }
    }

    override suspend fun inspect(cookie: String): AppResult<Identity> {
        val reply = client.send("auth/session", cookie = cookie)
        val identity = identity(reply.body)
        return if (reply.status == 200 && identity != null) {
            AppResult.Success(identity)
        } else {
            AppResult.Failure(reply.error ?: AppError.Unknown())
        }
    }

    override suspend fun logout(cookie: String): AppResult<Unit> {
        val reply = client.send("auth/logout", ByteArray(0).toRequestBody(), cookie)
        return if (reply.status in setOf(204, 401)) AppResult.Success(Unit) else AppResult.Failure(reply.error ?: AppError.Unknown())
    }

    private fun identity(body: JsonObject?): Identity? {
        val user = body?.get("user") as? JsonObject ?: return null
        val name = (user["username"] as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull ?: return null
        val avatar = user["avatar_url"] as? JsonPrimitive ?: return null
        if (avatar != JsonNull && !avatar.isString) return null
        val password = (user["has_password"] as? JsonPrimitive)?.takeUnless { it.isString }?.booleanOrNull ?: return null
        val expires = (body["expires_at"] as? JsonPrimitive)?.contentOrNull ?: return null
        return try {
            Identity(name, avatar.contentOrNull, password, Instant.parse(expires))
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
