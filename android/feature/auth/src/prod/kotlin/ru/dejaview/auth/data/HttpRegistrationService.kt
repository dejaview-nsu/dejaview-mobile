package ru.dejaview.auth.data

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.dejaview.auth.domain.RegistrationService
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.network.BackendClient

internal class HttpRegistrationService(private val client: BackendClient) : RegistrationService {
    override suspend fun register(
        username: String,
        email: String,
        birthYear: Int,
        password: String,
    ): AppResult<Int> {
        val form = buildJsonObject {
            put("username", username)
            put("email", email)
            put("birth_year", birthYear)
            put("password", password)
        }
        return submit("auth/register", form, 201)
    }

    override suspend fun resend(email: String): AppResult<Int> =
        submit("auth/resend-confirmation", buildJsonObject { put("login", email) }, 202)

    private suspend fun submit(path: String, form: JsonObject, status: Int): AppResult<Int> {
        val response = client.send(path, form.toString().toRequestBody("application/json".toMediaType()))
        response.error?.let { return AppResult.Failure(it) }
        val seconds = (response.body?.get("resend_after") as? JsonPrimitive)?.takeUnless { it.isString }?.intOrNull
        return if (response.status == status && seconds != null && seconds in 0..60) {
            AppResult.Success(seconds)
        } else {
            AppResult.Failure(AppError.Unknown("INVALID_RESPONSE"))
        }
    }
}
