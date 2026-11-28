package ru.dejaview.audio.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import ru.dejaview.audio.domain.AudioSearch
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.contracts.session.SessionCookieStore
import ru.dejaview.media.AudioDraftStore
import ru.dejaview.model.MovieSummary
import ru.dejaview.network.BackendClient

@Module
@InstallIn(SingletonComponent::class)
internal object AudioBackend {
    @Provides fun search(
        client: BackendClient,
        cookies: SessionCookieStore,
        store: AudioDraftStore,
    ): AudioSearch = audioSearch(client, cookies) { id -> store.file(id).inputStream() }
}

internal fun audioSearch(
    client: BackendClient,
    cookies: SessionCookieStore,
    open: (String) -> java.io.InputStream,
): AudioSearch =
    AudioSearch { media, progress ->
        val body =
            object : RequestBody() {
                override fun contentType() = media.mimeType.toMediaType()

                override fun contentLength() = media.byteCount

                override fun isOneShot() = true

                override fun writeTo(sink: BufferedSink) {
                    open(media.id).use { input ->
                        val buffer = ByteArray(16 * 1024)
                        var sent = 0L
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            sink.write(buffer, 0, count)
                            sent += count
                            progress(sent)
                        }
                    }
                }
            }
        val multipart =
            MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", media.displayName, body)
                .build()
        val reply = client.sendProtected("search/audio", multipart, cookies)
        reply.error?.let { return@AudioSearch AppResult.Failure(it) }
        if (reply.status != 200) return@AudioSearch AppResult.Failure(AppError.Unknown())
        parseResults(reply.body)
    }

internal fun parseResults(body: JsonObject?): AppResult<List<MovieSummary>> {
    val bad = AppResult.Failure(AppError.Unknown("INVALID_RESPONSE"))
    val results = body?.get("results") as? JsonArray ?: return bad
    if (results.size > 1) return bad
    val movies =
        results.map {
            val obj = it as? JsonObject ?: return bad
            val id = (obj["movie_id"] as? JsonPrimitive)?.takeUnless { p -> p.isString }?.longOrNull ?: return bad
            val title = (obj["title"] as? JsonPrimitive)?.takeIf { p -> p.isString }?.content ?: return bad
            val year = obj["year"] as? JsonPrimitive ?: return bad
            val poster = obj["poster_url"] as? JsonPrimitive ?: return bad
            val overview = obj["overview"] as? JsonPrimitive ?: return bad
            if (year != JsonNull && (year.isString || year.intOrNull == null)) return bad
            if (poster != JsonNull && !poster.isString || overview != JsonNull && !overview.isString) return bad
            MovieSummary(id, title, year.intOrNull, poster.contentOrNull, overview.contentOrNull)
        }
    return AppResult.Success(movies)
}
