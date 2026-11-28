package ru.dejaview.search.text.reference

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.model.MovieSummary
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode
import ru.dejaview.search.text.reference.data.FakeReferenceSearch
import ru.dejaview.search.text.reference.presentation.ReferenceSearchAction
import ru.dejaview.search.text.reference.presentation.ReferenceSearchViewModel
import ru.dejaview.search.text.reference.presentation.SearchStatus
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
class ReferenceSearchTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun validatesUnicodeAndRestoresBoundedDraft() =
        runTest {
            val savedState = SavedStateHandle()
            val vm = ReferenceSearchViewModel(FakeReferenceSearch(), savedState)

            vm.edit("  аа  ")
            vm.submit()
            assertEquals(SearchStatus.Editing, vm.state.value.status)
            assertFalse(vm.state.value.canSubmit)

            vm.edit("🎬".repeat(250))
            assertEquals(250, vm.state.value.characterCount)
            assertTrue(vm.state.value.canSubmit)

            vm.edit("🎬".repeat(10_000))
            assertEquals(251, vm.state.value.characterCount)
            assertFalse(vm.state.value.canSubmit)
            assertEquals(vm.state.value, ReferenceSearchViewModel(FakeReferenceSearch(), savedState).state.value)
        }

    @Test
    fun validatesTrimmedBoundariesAndSendsTheSameUnicodeQuery() =
        runTest {
            val requests = mutableListOf<String>()
            val vm =
                ReferenceSearchViewModel(
                    { query ->
                        requests += query
                        AppResult.Success(emptyList())
                    },
                    SavedStateHandle(),
                )
            val invalid = listOf("", "\u0085\u00A0\u3000", "🎬a", "a".repeat(251), "a".repeat(250) + " ".repeat(300) + "z")
            invalid.forEach { query ->
                vm.edit(query)
                assertFalse(vm.state.value.canSubmit)
                vm.submit()
            }
            val valid = listOf("Сюж", "A🎬Z", "🎬".repeat(250), "\u001Cяa")
            valid.forEach { query ->
                vm.edit("\u0085\u00A0" + query + "\u202F\u3000")
                assertEquals(query.codePointCount(0, query.length), vm.state.value.characterCount)
                assertTrue(vm.state.value.canSubmit)
                vm.submit()
                advanceUntilIdle()
            }
            assertEquals(valid, requests)
        }

    @Test
    fun publishesSuccessfulAndEmptySearchesToTheShellWithTheDraftIntact() =
        runTest {
            val vm = ReferenceSearchViewModel(FakeReferenceSearch(), SavedStateHandle())
            val delivered = mutableListOf<SearchCompletion>()

            vm.edit("  космос  ")
            vm.onAction(ReferenceSearchAction.Submit, delivered::add)
            assertEquals(SearchStatus.Loading, vm.state.value.status)
            advanceUntilIdle()
            assertEquals(SearchStatus.Editing, vm.state.value.status)
            assertEquals("  космос  ", vm.state.value.query)

            vm.edit("несуществующий сюжет")
            vm.onAction(ReferenceSearchAction.Submit, delivered::add)
            advanceUntilIdle()
            assertEquals(listOf(1, 0), delivered.map { it.results.size })
            assertEquals(
                "Сигнал с далёкой орбиты",
                delivered
                    .first()
                    .results
                    .single()
                    .title,
            )
            assertEquals(SearchMode.TEXT, delivered.first().mode)
            assertEquals("космос", delivered.first().inputSummary)
            assertEquals(2, delivered.map { it.requestId }.distinct().size)
        }

    @Test
    fun mapsFailuresAndRetriesTheSameQueryOnlyOnce() =
        runTest {
            val errors = ArrayDeque(listOf(AppError.Offline(), AppError.ServiceUnavailable(), AppError.RateLimit(12)))
            val requests = mutableListOf<String>()
            val vm =
                ReferenceSearchViewModel(
                    search = { query ->
                        requests += query
                        AppResult.Failure(errors.removeFirst())
                    },
                    savedState = SavedStateHandle(),
                )

            vm.edit("  космос   ")
            vm.submit()
            vm.submit()
            advanceUntilIdle()
            assertEquals(SearchStatus.Offline, vm.state.value.status)
            assertEquals(listOf("космос"), requests)

            vm.submit()
            advanceUntilIdle()
            assertEquals(SearchStatus.Failed, vm.state.value.status)

            vm.submit()
            advanceUntilIdle()
            assertEquals(SearchStatus.RateLimited(12), vm.state.value.status)
            assertEquals(List(3) { "космос" }, requests)
        }

    @Test
    fun ignoresLateResponseAfterCancelOrEditAndNewSubmit() =
        runTest {
            val answers = mutableListOf<Continuation<AppResult<List<MovieSummary>>>>()
            val delivered = mutableListOf<SearchCompletion>()
            val vm =
                ReferenceSearchViewModel(
                    search = { suspendCoroutine { answers += it } },
                    savedState = SavedStateHandle(),
                )
            vm.edit("космос")
            vm.onAction(ReferenceSearchAction.Submit, delivered::add)
            runCurrent()
            vm.onAction(ReferenceSearchAction.Cancel, delivered::add)
            answers[0].resume(AppResult.Success(listOf(MovieSummary(1, "Старый результат"))))
            runCurrent()
            assertEquals(SearchStatus.Editing, vm.state.value.status)

            vm.onAction(ReferenceSearchAction.Submit, delivered::add)
            runCurrent()
            vm.edit("поезд")
            vm.onAction(ReferenceSearchAction.Submit, delivered::add)
            runCurrent()
            answers[1].resume(AppResult.Success(listOf(MovieSummary(1, "Старый результат"))))
            runCurrent()
            assertEquals(SearchStatus.Loading, vm.state.value.status)
            val movie = MovieSummary(2, "Новый результат")
            answers[2].resume(AppResult.Success(listOf(movie)))
            runCurrent()
            assertEquals(SearchStatus.Editing, vm.state.value.status)
            assertEquals(listOf(movie), delivered.single().results)
        }

    private fun ReferenceSearchViewModel.edit(query: String) = onAction(ReferenceSearchAction.Edit(query)) {}

    private fun ReferenceSearchViewModel.submit() = onAction(ReferenceSearchAction.Submit) {}
}
