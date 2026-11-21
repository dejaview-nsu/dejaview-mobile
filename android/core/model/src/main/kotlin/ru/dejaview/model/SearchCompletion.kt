package ru.dejaview.model

import java.time.Instant

data class SearchCompletion(
    val requestId: RequestId,
    val mode: SearchMode,
    val results: List<MovieSummary>,
    val inputSummary: String,
    val completedAt: Instant,
)
