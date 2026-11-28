package ru.dejaview.app

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingFlowTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test
    fun firstLaunchOpensOnboardingAndAuthThenLaterLaunchOpensHome() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences(STARTUP_PREFERENCES, Context.MODE_PRIVATE)
        preferences.edit().remove(INTRODUCTION_STARTED).commit()
        try {
            ActivityScenario.launch(MainActivity::class.java).use {
                awaitText("Видели кусочек фильма? Найдём целиком")
                compose.onNodeWithText("Войти или создать аккаунт").performScrollTo().performClick()
                awaitText("С возвращением")
                compose.onNodeWithText("Создать аккаунт").performScrollTo().performClick()
                compose.onNodeWithText("Регистрация").assertIsDisplayed()
                for ((label, value) in listOf(
                    "Имя пользователя" to "viewer_demo",
                    "Электронная почта" to "viewer@example.test",
                    "Год рождения" to "1998",
                    "Пароль" to "Cinema!2000",
                    "Повторите пароль" to "Cinema!2000",
                )) {
                    compose.onNodeWithContentDescription(label).performScrollTo().performTextInput(value)
                }
                compose.onNodeWithText("Создать аккаунт").performScrollTo().performClick()
                awaitText("Проверьте почту")
                assertTrue(preferences.getBoolean(INTRODUCTION_STARTED, false))
            }
            ActivityScenario.launch(MainActivity::class.java).use {
                awaitText("Найди фильм по обрывку памяти")
                compose.onNodeWithText("Описание").assertIsDisplayed()
                compose.onNodeWithText("Видели кусочек фильма? Найдём целиком").assertDoesNotExist()
            }
        } finally {
            preferences.edit().putBoolean(INTRODUCTION_STARTED, true).commit()
        }
    }

    private fun awaitText(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }
    }
}
