package ru.dejaview.navigation

import ru.dejaview.model.MovieId
import ru.dejaview.model.MovieList
import ru.dejaview.model.SearchMode
import java.util.UUID

sealed interface PendingAction {
    data class OpenMode(
        val mode: SearchMode,
        val draftId: String? = null,
    ) : PendingAction

    data class SetMembership(
        val movieId: MovieId,
        val list: MovieList,
        val desiredValue: Boolean,
    ) : PendingAction

    data class SetRating(
        val movieId: MovieId,
        val rating: Int?,
    ) : PendingAction
}

// Owned by the navigation host. Pending mutations are intentionally process-local.
class PendingActionStore {
    private var pending: Pair<String, PendingAction>? = null

    fun expect(action: PendingAction): String = UUID.randomUUID().toString().also { pending = it to action }

    fun consume(expectedId: String?): PendingAction? {
        val current = pending ?: return null
        if (current.first != expectedId) return null
        pending = null
        return current.second
    }

    fun clear() {
        pending = null
    }
}
