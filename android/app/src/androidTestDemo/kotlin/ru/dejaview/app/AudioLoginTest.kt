package ru.dejaview.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AudioLoginTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun guest() {
        runBlocking {
            compose.activity.sessions.controller
                .logout()
        }
    }

    @Test fun cancelThenLoginReturnsToAudioAndLogoutClearsAccess() {
        click("Звук")
        click("Отмена")
        compose.onNodeWithText("Описание").assertIsDisplayed()
        click("Звук")
        compose.onNodeWithContentDescription("Имя пользователя или email").performTextInput("viewer")
        compose.onNodeWithContentDescription("Пароль").performTextInput("x")
        click("Войти")
        compose.waitUntil(5000) {
            compose
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasText("Выбрать файл"),
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("Выбрать файл").assertIsDisplayed()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        click("Профиль")
        compose.onNodeWithText("viewer").assertIsDisplayed()
        click("Выйти")
        click("Звук")
        compose.onNodeWithText("Вход").assertIsDisplayed()
    }

    private fun click(text: String) {
        compose.onNodeWithText(text).performClick()
    }
}
