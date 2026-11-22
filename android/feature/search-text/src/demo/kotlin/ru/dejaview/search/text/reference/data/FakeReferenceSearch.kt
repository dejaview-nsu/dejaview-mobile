package ru.dejaview.search.text.reference.data

import kotlinx.coroutines.delay
import ru.dejaview.contracts.AppResult
import ru.dejaview.model.MovieSummary
import ru.dejaview.search.text.reference.domain.ReferenceSearch
import kotlin.time.Duration.Companion.milliseconds

internal class FakeReferenceSearch : ReferenceSearch {
    override suspend fun search(query: String): AppResult<List<MovieSummary>> {
        delay(SIMULATED_SEARCH_DELAY)
        val words = query.split(' ').filter { it.length >= MIN_SEARCH_WORD_LENGTH }
        return AppResult.Success(
            catalog.filter { movie ->
                words.any { word ->
                    movie.title.contains(word, ignoreCase = true) ||
                        movie.overview.orEmpty().contains(word, ignoreCase = true)
                }
            },
        )
    }

    private companion object {
        const val MIN_SEARCH_WORD_LENGTH = 3
        val SIMULATED_SEARCH_DELAY = 650.milliseconds

        val catalog =
            listOf(
                MovieSummary(1, "Сигнал с далёкой орбиты", 2024, overview = "Пилот отправляется в космос и ловит незнакомый сигнал."),
                MovieSummary(2, "Последний ночной поезд", 2023, overview = "Попутчики пытаются разгадать тайну исчезнувшего поезда."),
            )
    }
}
