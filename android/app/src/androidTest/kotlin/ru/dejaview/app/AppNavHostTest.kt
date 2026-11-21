package ru.dejaview.app

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.dejaview.model.MovieList
import ru.dejaview.model.SearchMode
import ru.dejaview.navigation.HistoryRoute
import ru.dejaview.navigation.LibraryRoute
import ru.dejaview.navigation.LoginRoute
import ru.dejaview.navigation.MovieRoute
import ru.dejaview.navigation.ProfileRoute
import ru.dejaview.navigation.ResultsRoute
import ru.dejaview.navigation.SearchRoute

class AppNavHostTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private lateinit var navController: TestNavHostController

    @Test
    fun launchesSearchWithBadgeOnlyInDemo() {
        assertScreen(R.string.search_placeholder)
        val badge = compose.onNodeWithText("Демо")
        if (compose.activity.packageName.endsWith(".demo")) {
            badge.assertIsDisplayed()
        } else {
            badge.assertDoesNotExist()
        }
        assertTrue(compose.activity.searchModes.isEmpty())
    }

    @Test
    fun registeredDestinationsAcceptTypedArguments() {
        compose.runOnUiThread {
            navController =
                TestNavHostController(compose.activity).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
            compose.activity.setContent {
                MaterialTheme {
                    AppNavHost(searchModes = emptyMap(), navController = navController)
                }
            }
        }
        assertDestination(SearchRoute(SearchMode.TEXT), R.string.search_placeholder)
        navigateTo(SearchRoute(SearchMode.IMAGE, "draft/?#Ю"), R.string.search_placeholder)
        navigateTo(ResultsRoute("request/?#Ю"), R.string.results_placeholder)
        navigateTo(MovieRoute(Long.MAX_VALUE), R.string.movie_placeholder)
        navigateTo(LoginRoute("action/?#Ю"), R.string.login_placeholder)
        navigateTo(LibraryRoute(MovieList.WATCHED), R.string.library_placeholder)
        navigateTo(ProfileRoute, R.string.profile_placeholder)
        navigateTo(HistoryRoute, R.string.history_placeholder)
        compose.runOnIdle { assertTrue(navController.popBackStack()) }
        assertDestination(ProfileRoute, R.string.profile_placeholder)
    }

    private inline fun <reified T : Any> navigateTo(
        route: T,
        title: Int,
    ) {
        compose.runOnIdle { navController.navigate(route) }
        assertDestination(route, title)
    }

    private inline fun <reified T : Any> assertDestination(
        route: T,
        title: Int,
    ) {
        assertScreen(title)
        compose.runOnIdle { assertEquals(route, navController.currentBackStackEntry?.toRoute<T>()) }
    }

    private fun assertScreen(title: Int) {
        compose.onNodeWithText(compose.activity.getString(title)).assertIsDisplayed()
    }
}
