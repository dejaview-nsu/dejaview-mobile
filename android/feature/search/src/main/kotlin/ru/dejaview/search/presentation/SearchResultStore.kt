package ru.dejaview.search.presentation

import ru.dejaview.model.RequestId
import ru.dejaview.model.SearchCompletion

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
