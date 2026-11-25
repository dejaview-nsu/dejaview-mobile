package ru.dejaview.search.presentation

import ru.dejaview.model.RequestId
import ru.dejaview.model.SearchCompletion
import ru.dejaview.navigation.SearchRoute
import java.util.UUID

internal class PendingActionStore {
    private val actions = mutableMapOf<String, SearchRoute>()

    fun put(route: SearchRoute): String = UUID.randomUUID().toString().also { actions[it] = route }

    fun take(id: String?): SearchRoute? = actions.remove(id)
}

internal class SearchResultStore {
    private val seenRequests = mutableSetOf<RequestId>()
    private val results = mutableMapOf<RequestId, SearchCompletion>()

    fun add(completion: SearchCompletion): Boolean {
        if (!seenRequests.add(completion.requestId)) return false
        results[completion.requestId] = completion
        return true
    }

    operator fun get(requestId: RequestId): SearchCompletion? = results[requestId]

    fun clear() = results.clear()
}
