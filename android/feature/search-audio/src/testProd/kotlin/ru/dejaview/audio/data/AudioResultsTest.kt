package ru.dejaview.audio.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.dejaview.contracts.AppResult
import ru.dejaview.model.MovieSummary

class AudioResultsTest {
    private fun parse(raw: String) = parseResults(Json.parseToJsonElement(raw).jsonObject)

    @Test fun returnsZeroOrOneFilmWithRealNullableFields() {
        assertEquals(AppResult.Success(emptyList<MovieSummary>()), parse("""{"results":[]}"""))
        val movie = """{"movie_id":12,"title":"Sample","year":null,"poster_url":null,"overview":null}"""
        assertEquals(AppResult.Success(listOf(MovieSummary(12, "Sample"))), parse("""{"results":[$movie]}"""))
        assertTrue(parse("""{"results":[$movie,$movie]}""") is AppResult.Failure)
        assertTrue(parse("""{"results":[{"movie_id":12}]}""") is AppResult.Failure)
        assertTrue(parse("""{}""") is AppResult.Failure)
    }
}
