package ru.dejaview.search.text.reference.domain

import ru.dejaview.contracts.AppResult
import ru.dejaview.model.MovieSummary

internal fun interface ReferenceSearch {
    suspend fun search(query: String): AppResult<List<MovieSummary>>
}
