package ru.dejaview.search.text.reference.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.model.MovieSummary
import ru.dejaview.network.BackendClient
import ru.dejaview.search.text.reference.domain.ReferenceSearch
import javax.inject.Inject

internal class HttpTextSearch
    @Inject
    constructor(
        private val client: BackendClient,
    ) : ReferenceSearch {
        override suspend fun search(query: String): AppResult<List<MovieSummary>> {
            val json = buildJsonObject { put("query", query) }
            val reply = client.send("search/text", json.toString().toRequestBody(JSON))
            reply.error?.let { return AppResult.Failure(it) }
            return if (reply.status == 200) parseTextResults(reply.body) else invalidResponse()
        }
    }

private val JSON = "application/json".toMediaType()
private const val MAX_RESULTS = 10

private fun parseTextResults(body: JsonObject?): AppResult<List<MovieSummary>> {
    val results = body?.get("results") as? JsonArray ?: return invalidResponse()
    if (results.size > MAX_RESULTS) return invalidResponse()
    return AppResult.Success(results.map { (it as? JsonObject)?.toMovie() ?: return invalidResponse() })
}

private fun JsonObject.toMovie(): MovieSummary? {
    val id = (get("movie_id") as? JsonPrimitive)?.takeUnless { it.isString }?.longOrNull ?: return null
    val title = (get("title") as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return null
    val year = get("year") as? JsonPrimitive ?: return null
    if (year != JsonNull && (year.isString || year.intOrNull == null)) return null
    val poster = get("poster_url") as? JsonPrimitive ?: return null
    val overview = get("overview") as? JsonPrimitive ?: return null
    if (!poster.isNullableString() || !overview.isNullableString()) return null
    return MovieSummary(id, title, year.intOrNull, poster.contentOrNull, overview.contentOrNull)
}

private fun JsonPrimitive.isNullableString() = this == JsonNull || isString

private fun invalidResponse() = AppResult.Failure(AppError.Unknown("INVALID_RESPONSE"))
