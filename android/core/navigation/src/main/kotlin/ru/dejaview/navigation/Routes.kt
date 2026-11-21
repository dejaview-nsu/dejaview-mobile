package ru.dejaview.navigation

import kotlinx.serialization.Serializable
import ru.dejaview.model.MovieId
import ru.dejaview.model.MovieList
import ru.dejaview.model.RequestId
import ru.dejaview.model.SearchMode

@Serializable
data class SearchRoute(
    val mode: SearchMode,
    val draftId: String? = null,
)

@Serializable
data class ResultsRoute(
    val requestId: RequestId,
)

@Serializable
data class MovieRoute(
    val movieId: MovieId,
)

@Serializable
data class LoginRoute(
    val pendingActionId: String? = null,
)

@Serializable
data class LibraryRoute(
    val list: MovieList,
)

@Serializable
data object ProfileRoute

@Serializable
data object HistoryRoute
