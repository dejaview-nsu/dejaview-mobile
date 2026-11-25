package ru.dejaview.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DemoSearchFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun beginAsGuest() {
        compose.runOnIdle {
            compose.activity.searchServices.defaults
                .expire()
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
    fun demoLoginReturnsToProtectedModeAndEmptyResultReturnsToTheSameMode() {
        compose.onNodeWithText("Видео").performClick()
        compose.onNodeWithText("Войти в деморежиме").performClick()
        waitForText("Показать результаты (0)")
        compose.onNodeWithText("Показать результаты (0)").performClick()
        waitForText("Вернуться к поиску")
        compose.onNodeWithText("Вернуться к поиску").performClick()
        compose.onNodeWithText("Показать результаты (0)").assertIsDisplayed()
    }

    private fun waitForText(text: String) {
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
