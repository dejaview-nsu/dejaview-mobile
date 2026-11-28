package ru.dejaview.navigation

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import ru.dejaview.model.MovieList
import ru.dejaview.model.SearchMode

class RoutesTest {
    @Test
    fun searchRoutePreservesModeAndOptionalDraft() {
        for (mode in SearchMode.entries) {
            assertRoundTrip(SearchRoute(mode))
            assertRoundTrip(SearchRoute(mode, "draft /?# Ю"))
        }
    }

    @Test
    fun searchAccessRoutePreservesTheRequestedMode() {
        for (mode in SearchMode.entries) {
            assertRoundTrip(SearchAccessRoute(mode))
        }
    }

    @Test
    fun loginRoutePreservesOptionalPendingAction() {
        assertRoundTrip(LoginRoute())
        assertRoundTrip(LoginRoute("action /?# Ю"))
    }

    @Test
    fun resultsAndMovieRoutesPreserveTheirIdentifiers() {
        assertRoundTrip(ResultsRoute("request /?# Ю"))
        assertRoundTrip(MovieRoute(Long.MAX_VALUE))
    }

    @Test
    fun libraryRoutePreservesEitherList() {
        for (list in MovieList.entries) {
            assertRoundTrip(LibraryRoute(list))
        }
    }

    @Test
    fun routesWithoutArgumentsRoundTrip() {
        assertRoundTrip(ProfileRoute)
        assertRoundTrip(HistoryRoute)
        assertRoundTrip(SearchHomeRoute)
        assertRoundTrip(OpeningRoute)
        assertRoundTrip(OnboardingRoute)
        assertRoundTrip(RegistrationRoute)
    }

    @Test
    fun routesRejectMissingRequiredArguments() {
        assertThrows(SerializationException::class.java) { Json.decodeFromString<SearchRoute>("{}") }
        assertThrows(SerializationException::class.java) { Json.decodeFromString<SearchAccessRoute>("{}") }
        assertThrows(SerializationException::class.java) { Json.decodeFromString<ResultsRoute>("{}") }
        assertThrows(SerializationException::class.java) { Json.decodeFromString<MovieRoute>("{}") }
        assertThrows(SerializationException::class.java) { Json.decodeFromString<LibraryRoute>("{}") }
    }

    private inline fun <reified T> assertRoundTrip(route: T) {
        assertEquals(route, Json.decodeFromString<T>(Json.encodeToString(route)))
        assertEquals(route, Json.decodeFromString<T>(jsonWithDefaults.encodeToString(route)))
    }

    private companion object {
        val jsonWithDefaults = Json { encodeDefaults = true }
    }
}
