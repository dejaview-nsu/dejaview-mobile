package ru.dejaview.search.text.reference.data

import kotlinx.coroutines.delay
import ru.dejaview.contracts.AppResult
import ru.dejaview.model.MovieSummary
import ru.dejaview.search.text.reference.domain.ReferenceSearch

internal class FakeReferenceSearch : ReferenceSearch {
    override suspend fun search(query: String): AppResult<List<MovieSummary>> {
        delay(650)
        val words = query.split(' ').filter { it.length >= 3 }
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
        val catalog =
            listOf(
                MovieSummary(1, "Сигнал с далёкой орбиты", 2024, overview = "Пилот отправляется в космос и ловит незнакомый сигнал."),
                MovieSummary(2, "Последний ночной поезд", 2023, overview = "Попутчики пытаются разгадать тайну исчезнувшего поезда."),
            )
    }
}
