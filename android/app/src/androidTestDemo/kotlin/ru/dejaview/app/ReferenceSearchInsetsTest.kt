package ru.dejaview.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReferenceSearchInsetsTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun searchButtonStaysAboveImeAndSubmitsTheDemoResult() {
        val field = compose.onNodeWithContentDescription("Описание фильма")
        val submit = compose.onNodeWithText("Найти фильм")
        val heading = compose.onNodeWithText("Поиск по описанию")
        val (safeTop, safeBottom) =
            compose.runOnUiThread {
                val bars = insets().getInsets(WindowInsetsCompat.Type.systemBars())
                bars.top to (compose.activity.window.decorView.height - bars.bottom)
            }
        val headingNode = heading.fetchSemanticsNode()
        assertTrue(headingNode.positionInWindow.y >= safeTop)
        val initialButton = submit.assertIsDisplayed().fetchSemanticsNode()
        assertTrue(initialButton.positionInWindow.y + initialButton.size.height <= safeBottom + 1)

        field.performClick().performTextInput("космос")
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.runOnUiThread { insets().isVisible(WindowInsetsCompat.Type.ime()) }
        }
        val keyboardTop =
            compose.runOnUiThread {
                compose.activity.window.decorView.height - insets().getInsets(WindowInsetsCompat.Type.ime()).bottom
            }
        val buttonNode = submit.assertIsDisplayed().fetchSemanticsNode()
        assertTrue(buttonNode.positionInWindow.y + buttonNode.size.height <= keyboardTop + 1)
        submit.performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            compose
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasText("Сигнал с далёкой орбиты"),
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("Сигнал с далёкой орбиты").performScrollTo().assertIsDisplayed()
    }

    private fun insets(): WindowInsetsCompat = checkNotNull(ViewCompat.getRootWindowInsets(compose.activity.window.decorView))
}
