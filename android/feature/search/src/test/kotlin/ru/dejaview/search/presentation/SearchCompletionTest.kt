package ru.dejaview.search.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.model.SearchMode

@OptIn(ExperimentalCoroutinesApi::class)
class SearchCompletionTest {
    @Before
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun deduplicatesAndKeepsAllResultsWhenHistoryWriteFails() =
        runTest {
            val history = TestHistory(fails = true)
            val session = TestSession()
            val vm = SearchViewModel(session, history)
            for (count in listOf(10, 1, 0)) {
                val result = syntheticCompletion(requestId = "search-$count", count = count)
                assertTrue(vm.complete(result, SearchMode.TEXT, session.state.value))
                assertFalse(vm.complete(result, SearchMode.TEXT, session.state.value))
                assertEquals(result, vm.result(result.requestId))
            }
            runCurrent()
            assertEquals(listOf(10, 1, 0), history.writes.map { it.second.results.size })
            assertTrue(history.writes.all { it.first == null })
        }

    @Test
    fun restoresFromCurrentAccountHistoryOrOffersRetry() =
        runTest {
            val owner = signedIn()
            val history = TestHistory()
            val result = syntheticCompletion()
            history.record(result, owner.accountScope)
            val session = TestSession(SessionState.Initializing)
            val newGraph = SearchViewModel(session, history)

            val restored = async { newGraph.result(result.requestId) }
            runCurrent()
            assertFalse(restored.isCompleted)
            session.state.value = owner
            assertEquals(result, restored.await())
            assertNull(newGraph.result("missing"))
            session.state.value = signedIn("another-scope")
            assertNull(newGraph.result(result.requestId))
        }

    @Test
    fun rejectsMismatchedModeAndLateCompletionFromOldSession() =
        runTest {
            val previous = signedIn()
            val session = TestSession(previous)
            val history = TestHistory()
            val vm = SearchViewModel(session, history)
            val result = syntheticCompletion(mode = SearchMode.VIDEO)
            assertFalse(vm.complete(result, SearchMode.IMAGE, previous))
            assertTrue(vm.complete(result, SearchMode.VIDEO, previous))
            runCurrent()
            session.state.value = SessionState.Expired
            assertFalse(vm.complete(result, SearchMode.VIDEO, previous))
            session.state.value = signedIn("another-scope")
            assertFalse(vm.complete(result, SearchMode.VIDEO, previous))
            assertNull(vm.result(result.requestId))
            assertEquals(listOf(previous.accountScope to result), history.writes)
        }
}
