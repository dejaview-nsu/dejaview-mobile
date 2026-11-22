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
    fun reportsLoadingThenResultsAndEmpty() =
        runTest {
            val vm = ReferenceSearchViewModel(FakeReferenceSearch(), SavedStateHandle())

            vm.edit("  космос  ")
            vm.submit()
            assertEquals(SearchStatus.Loading, vm.state.value.status)
            advanceUntilIdle()
            val results = vm.state.value.status as SearchStatus.Results
            assertEquals("Сигнал с далёкой орбиты", results.movies.single().title)

            vm.edit("несуществующий сюжет")
            vm.submit()
            advanceUntilIdle()
            assertEquals(SearchStatus.Empty, vm.state.value.status)
        }

    @Test
    fun mapsFailuresAndRetriesTheSameQueryOnlyOnce() =
        runTest {
            val errors = ArrayDeque(listOf(AppError.Offline(), AppError.ServiceUnavailable()))
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
            assertEquals(listOf("космос", "космос"), requests)
        }

    @Test
    fun ignoresLateResponseAfterCancelOrEditAndNewSubmit() =
        runTest {
            val answers = mutableListOf<Continuation<AppResult<List<MovieSummary>>>>()
            val vm =
                ReferenceSearchViewModel(
                    search = { suspendCoroutine { answers += it } },
                    savedState = SavedStateHandle(),
                )
            vm.edit("космос")
            vm.submit()
            runCurrent()
            vm.onAction(ReferenceSearchAction.Cancel)
            answers[0].resume(AppResult.Success(listOf(MovieSummary(1, "Старый результат"))))
            runCurrent()
            assertEquals(SearchStatus.Editing, vm.state.value.status)

            vm.submit()
            runCurrent()
            vm.edit("поезд")
            vm.submit()
            runCurrent()
            answers[1].resume(AppResult.Success(listOf(MovieSummary(1, "Старый результат"))))
            runCurrent()
            assertEquals(SearchStatus.Loading, vm.state.value.status)
            val movie = MovieSummary(2, "Новый результат")
            answers[2].resume(AppResult.Success(listOf(movie)))
            runCurrent()
            assertEquals(SearchStatus.Results(listOf(movie)), vm.state.value.status)
        }

    private fun ReferenceSearchViewModel.edit(query: String) = onAction(ReferenceSearchAction.Edit(query))

    private fun ReferenceSearchViewModel.submit() = onAction(ReferenceSearchAction.Submit)
}
