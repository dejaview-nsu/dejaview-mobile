package ru.dejaview.search.text.domain

import ru.dejaview.contracts.AppResult
import ru.dejaview.model.MovieSummary

internal fun interface TextSearch {
    suspend fun search(query: String): AppResult<List<MovieSummary>>
}
