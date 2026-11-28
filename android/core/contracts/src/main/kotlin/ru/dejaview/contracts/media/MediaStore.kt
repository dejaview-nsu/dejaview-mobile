package ru.dejaview.contracts.media

import ru.dejaview.contracts.AppResult

data class PreparedMedia(
    val id: String,
    val mimeType: String,
    val byteCount: Long,
    val durationMs: Long?,
    val displayName: String,
)

interface MediaStore {
    suspend fun metadata(id: String): AppResult<PreparedMedia>

    suspend fun release(id: String)
}
