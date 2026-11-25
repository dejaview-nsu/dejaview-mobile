package ru.dejaview.app

import androidx.compose.ui.test.SemanticsNodeInteraction
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

private const val UI_TIMEOUT_MS = 10_000L
private const val INSET_ROUNDING_TOLERANCE_PX = 1
private const val DEMO_MOVIE_TITLE = "Сигнал с далёкой орбиты"

class ReferenceSearchInsetsTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun searchButtonStaysAboveImeAndSubmitsTheDemoResult() {
        compose.onNodeWithText("Описание").performClick()
        val field = compose.onNodeWithContentDescription("Описание фильма")
        val submit = compose.onNodeWithText("Найти фильм")
        assertHeadingBelowSystemBars()
        assertAboveInset(submit, WindowInsetsCompat.Type.systemBars())

        field.performClick().performTextInput("космос")
        compose.waitUntil(timeoutMillis = UI_TIMEOUT_MS) {
            compose.runOnUiThread { insets().isVisible(WindowInsetsCompat.Type.ime()) }
        }
        assertAboveInset(submit, WindowInsetsCompat.Type.ime())

        submit.performClick()
        compose.waitUntil(timeoutMillis = UI_TIMEOUT_MS) {
            compose.onAllNodes(hasText(DEMO_MOVIE_TITLE)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(DEMO_MOVIE_TITLE).performScrollTo().assertIsDisplayed()
    }

    private fun assertHeadingBelowSystemBars() {
        val safeTop =
            compose.runOnUiThread { insets().getInsets(WindowInsetsCompat.Type.systemBars()).top }
        val heading = compose.onNodeWithText("Поиск по описанию").fetchSemanticsNode()
        assertTrue(heading.positionInWindow.y >= safeTop)
    }

    private fun assertAboveInset(
        control: SemanticsNodeInteraction,
        insetTypes: Int,
    ) {
        val insetTop =
            compose.runOnUiThread {
                compose.activity.window.decorView.height - insets().getInsets(insetTypes).bottom
            }
        val node = control.assertIsDisplayed().fetchSemanticsNode()
        val controlBottom = node.positionInWindow.y + node.size.height
        assertTrue(controlBottom <= insetTop + INSET_ROUNDING_TOLERANCE_PX)
    }

    private fun insets(): WindowInsetsCompat = checkNotNull(ViewCompat.getRootWindowInsets(compose.activity.window.decorView))
}
