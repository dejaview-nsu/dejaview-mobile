package ru.dejaview.model

typealias MovieId = Long

typealias RequestId = String

data class MovieSummary(
    val movieId: MovieId,
    val title: String,
    val year: Int? = null,
    val posterUrl: String? = null,
    val overview: String? = null,
)

enum class SearchMode { TEXT, IMAGE, VIDEO, AUDIO }
