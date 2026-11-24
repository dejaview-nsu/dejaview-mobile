package ru.dejaview.audio.domain

import ru.dejaview.contracts.AppResult
import ru.dejaview.contracts.media.PreparedMedia
import ru.dejaview.model.MovieSummary

internal fun interface AudioSearch {
    suspend fun search(
        media: PreparedMedia,
        progress: (Long) -> Unit,
    ): AppResult<List<MovieSummary>>
}
