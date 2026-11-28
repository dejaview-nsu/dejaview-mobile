package ru.dejaview.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.model.SearchMode
import ru.dejaview.core.ui.theme.DejaViewTheme
import ru.dejaview.navigation.MovieRoute

class DemoSearchFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun beginAsGuest() {
        runBlocking {
            compose.activity.sessions.controller
                .logout()
        }
    }

    @Test
    fun guestTextResultSurvivesRecreationAndBackKeepsTheDraft() {
        compose.onNodeWithText("Описание").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("космос")
        compose.onNodeWithText("Найти фильм").performClick()
        waitForText("Сигнал с далёкой орбиты")

        compose.activityRule.scenario.recreate()
        waitForText("Сигнал с далёкой орбиты")
        compose.onNodeWithText("Это не тот фильм").performClick()
        compose.onNode(hasSetTextAction()).assertTextContains("космос", substring = true)
    }

    @Test
    fun clearsDescriptionAndReturnsToTheEmptySearchDraft() {
        compose.onNodeWithText("Описание").performClick()
        val field = compose.onNodeWithContentDescription("Описание фильма")
        field.performTextInput("космос")
        compose.onNodeWithContentDescription("Очистить описание").performClick()
        compose.onNodeWithText("Найти фильм").assertIsNotEnabled()

        field.performTextInput("a synthetic query without matches")
        compose.onNodeWithText("Найти фильм").performClick()
        waitForText("Ничего не нашли")
        compose.onNodeWithText("Уточнить описание").performClick()
        field.assertTextContains("a synthetic query without matches", substring = true)

        compose.onNodeWithText("Найти фильм").performClick()
        waitForText("Искать по кадру или звуку")
        compose.onNodeWithText("Искать по кадру или звуку").performClick()
        compose.onNodeWithText("Кадр").assertIsDisplayed()
    }

    @Test
    fun opensMovieRouteWithTheSelectedMovieId() {
        lateinit var nav: NavHostController
        compose.runOnUiThread {
            val activity = compose.activity
            activity.setContent {
                nav = rememberNavController()
                DejaViewTheme {
                    AppNavHost(activity.searchModes, activity.searchServices, activity.sessions.controller, nav)
                }
            }
        }
        compose.onNodeWithText("Описание").performClick()
        compose.onNodeWithContentDescription("Описание фильма").performTextInput("поезд космос")
        compose.onNodeWithText("Найти фильм").performClick()
        waitForText("Последний ночной поезд")
        compose.onNodeWithText("Последний ночной поезд").performClick()
        compose.runOnIdle {
            assertEquals(2L, requireNotNull(nav.currentBackStackEntry).toRoute<MovieRoute>().movieId)
        }
    }

    @Test
    fun demoLoginReturnsToProtectedModeAndEmptyResultReturnsToTheSameMode() {
        compose.onNodeWithText("Видео").performClick()
        compose.onNodeWithContentDescription("Имя пользователя или email").performTextInput("viewer")
        compose.onNodeWithContentDescription("Пароль").performTextInput("x")
        compose.onNodeWithText("Войти").performClick()
        waitForText("Показать результаты (0)")
        compose.onNodeWithText("Показать результаты (0)").performClick()
        waitForText("Вернуться к поиску")
        val title = compose.onNodeWithText("Ничего не нашли").fetchSemanticsNode().boundsInRoot
        val viewport = compose.onRoot().fetchSemanticsNode().boundsInRoot
        assertTrue(title.center.y > viewport.top + viewport.height / 4)
        compose.onNodeWithText("Вернуться к поиску").performClick()
        compose.onNodeWithText("Показать результаты (0)").assertIsDisplayed()

        val services = compose.activity.searchServices
        val scope = (services.session.state.value as SessionState.Authenticated).accountScope
        runBlocking {
            val entry = withTimeout(5000) { services.history.observe(scope).first { it.isNotEmpty() } }.single()
            assertTrue(entry.mode == SearchMode.VIDEO && entry.results.isEmpty())
        }
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Профиль").performClick()
        compose.onNodeWithText("Выйти").performClick()
        runBlocking { withTimeout(5000) { services.history.observe(scope).first { it.isEmpty() } } }
    }

    private fun waitForText(text: String) {
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
