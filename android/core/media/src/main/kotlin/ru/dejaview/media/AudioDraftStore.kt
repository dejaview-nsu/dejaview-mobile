package ru.dejaview.media

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.contracts.media.MediaStore
import ru.dejaview.contracts.media.PreparedMedia
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

const val AUDIO_BYTES = 20L * 1024 * 1024
const val AUDIO_MS = 10_000L

internal fun validateAudio(
    size: Long,
    duration: Long?,
): String? =
    when {
        size <= 0 -> "FILE_CORRUPTED"
        size > AUDIO_BYTES -> "FILE_TOO_LARGE"
        duration == null || duration <= 0 -> "FILE_CORRUPTED"
        duration > AUDIO_MS -> "AUDIO_TOO_LONG"
        else -> null
    }

class AudioDraftStore(
    private val context: Context,
) : MediaStore {
    private val directory by lazy {
        File(context.cacheDir, "audio-drafts").apply {
            mkdirs()
            listFiles()?.filter { it.isFile }?.forEach { it.delete() }
        }
    }
    private val drafts = ConcurrentHashMap<String, Pair<PreparedMedia, File>>()

    fun temporary(): File = File.createTempFile("draft-", ".tmp", directory)

    suspend fun importDocument(uri: Uri): AppResult<PreparedMedia> {
        if (uri.scheme != "content") return invalid("FILE_ACCESS")
        val file = temporary()
        try {
            val input = context.contentResolver.openInputStream(uri) ?: return invalid("FILE_CORRUPTED")
            var size = 0L
            input.use { source ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = source.read(buffer)
                        if (count == -1) break
                        size += count
                        if (size > AUDIO_BYTES) return invalid("FILE_TOO_LARGE")
                        output.write(buffer, 0, count)
                    }
                }
            }
            return register(file)
        } finally {
            discardUnregistered(file)
        }
    }

    fun register(file: File): AppResult<PreparedMedia> {
        require(file.parentFile == directory)
        var registered = false
        try {
            val reader = MediaMetadataRetriever()
            val (mime, duration) =
                try {
                    reader.setDataSource(file.absolutePath)
                    if (reader.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) == "yes" ||
                        reader.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) != "yes"
                    ) {
                        return invalid("UNSUPPORTED_FORMAT")
                    }
                    val mime =
                        when (reader.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)) {
                            "audio/mpeg" -> "audio/mpeg"
                            "audio/x-wav", "audio/wav" -> "audio/x-wav"
                            "application/ogg", "audio/ogg" -> "audio/ogg"
                            "audio/mp4", "audio/x-m4a", "video/mp4" -> "audio/mp4"
                            else -> return invalid("UNSUPPORTED_FORMAT")
                        }
                    mime to reader.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                } finally {
                    reader.release()
                }
            validateAudio(file.length(), duration)?.let { return invalid(it) }
            val ext =
                when (mime) {
                    "audio/mpeg" -> "mp3"
                    "audio/x-wav" -> "wav"
                    "audio/ogg" -> "ogg"
                    else -> "m4a"
                }
            val item = PreparedMedia(UUID.randomUUID().toString(), mime, file.length(), duration, "audio." + ext)
            drafts[item.id] = item to file
            registered = true
            return AppResult.Success(item)
        } catch (_: IllegalArgumentException) {
            return invalid("FILE_CORRUPTED")
        } catch (_: RuntimeException) {
            return invalid("FILE_CORRUPTED")
        } finally {
            if (!registered) file.delete()
        }
    }

    fun file(id: String): File = drafts[id]?.second ?: throw IOException("Draft expired")

    fun discardUnregistered(file: File) {
        require(file.parentFile == directory)
        if (drafts.values.none { it.second == file }) file.delete()
    }

    override suspend fun metadata(id: String): AppResult<PreparedMedia> =
        drafts[id]?.first?.let { AppResult.Success(it) } ?: invalid("FILE_REQUIRED")

    override suspend fun release(id: String) {
        discard(id)
    }

    fun discard(id: String) {
        drafts.remove(id)?.second?.delete()
    }

    private fun invalid(code: String) = AppResult.Failure(AppError.InvalidFile(code))
}
