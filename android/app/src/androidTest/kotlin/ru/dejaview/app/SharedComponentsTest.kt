package ru.dejaview.app

import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.dejaview.core.ui.components.ContentFeedback
import ru.dejaview.core.ui.components.DejaViewTextField
import ru.dejaview.core.ui.components.FeedbackKind
import ru.dejaview.core.ui.components.MovieCard
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.components.SearchModeSelector
import ru.dejaview.core.ui.components.SearchStatusPage
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewTheme
import ru.dejaview.model.MovieSummary
import ru.dejaview.model.SearchMode

class SharedComponentsTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun selectionAndTextFieldAreOperableWithSemantics() {
        render {
            var mode by remember { mutableStateOf(SearchMode.TEXT) }
            var query by remember { mutableStateOf("") }
            Column {
                SearchModeSelector(mode, { mode = it })
                DejaViewTextField(query, { query = it }, label = "Эталонное поле")
                PrimaryButton("Отправить", {}, enabled = query.length >= 3)
            }
        }
        compose.onNodeWithText("Описание").assertIsSelected()
        compose.onNodeWithText("Кадр").performClick().assertIsSelected()
        compose.onNodeWithText("Отправить").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Эталонное поле").performTextInput("кино")
        compose.onNodeWithText("кино").assertIsDisplayed()
    }

    @Test
    fun movieTitleWrapsAt280DpAnd200PercentWithMissingPoster() {
        val title = "Путешествие к далёкой звезде и неожиданное возвращение неизвестного пилота"
        render {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 2f)) {
                MovieCard(MovieSummary(1, title), Modifier.width(280.dp), highlighted = true)
            }
        }
        compose.onNodeWithContentDescription("Постер недоступен").assertIsDisplayed()
        val layoutResults = mutableListOf<TextLayoutResult>()
        compose
            .onNodeWithText(title)
            .assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layoutResults) }
        assertTrue(layoutResults.single().lineCount > 1)
        assertFalse(layoutResults.single().hasVisualOverflow)
    }

    @Test
    fun feedbackAnnouncesNetworkAndFileErrorsAndInvokesProvidedAction() {
        var actions = 0
        render {
            var kind by remember { mutableStateOf(FeedbackKind.Offline) }
            ContentFeedback(kind, onAction = {
                actions++
                kind = FeedbackKind.InvalidFile
            })
        }
        compose.onNodeWithText("Нет подключения к интернету").assertIsDisplayed()
        compose.onNodeWithText("Повторить").performClick()
        compose.onNodeWithText("Этот формат не подходит").assertIsDisplayed()
        compose.onNodeWithText("Выбрать другой файл").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, actions) }
    }

    @Test
    fun screenshotPreservesPrimaryButtonFillAndRoundedCorners() {
        render {
            Column(Modifier.background(DejaViewColors.background)) {
                PrimaryButton("Найти фильм", {}, Modifier.width(240.dp).testTag("primary"))
            }
        }
        val image = compose.onNodeWithTag("primary").captureToImage().toPixelMap()
        val background = Color(0xFF15100C)
        val accent = Color(0xFFE8AE3A)
        assertEquals(background, image[1, 1])
        assertEquals(background, image[image.width - 2, image.height - 2])
        assertEquals(accent, image[image.width / 8, image.height / 2])
        assertEquals(accent, image[image.width * 7 / 8, image.height / 2])
    }

    @Test
    fun compactOfflinePageKeepsBothActionsBelowTheMessageAtLargeFontScale() {
        var actions = 0
        render {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 2f)) {
                SearchStatusPage(
                    FeedbackKind.Offline,
                    "Поиск по описанию",
                    onPrimary = { actions++ },
                    onSecondary = { actions += 2 },
                    modifier = Modifier.size(360.dp, 640.dp),
                )
            }
        }
        val title =
            compose
                .onNodeWithText("Нет подключения к интернету")
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .boundsInRoot
        val retry = compose.onNodeWithText("Повторить").assertIsDisplayed()
        val home = compose.onNodeWithText("На главную").assertIsDisplayed()
        assertTrue(title.bottom < retry.fetchSemanticsNode().boundsInRoot.top)
        assertTrue(retry.fetchSemanticsNode().boundsInRoot.bottom < home.fetchSemanticsNode().boundsInRoot.top)
        retry.performClick()
        home.performClick()
        compose.runOnIdle { assertEquals(3, actions) }
    }

    private fun render(content: @Composable () -> Unit) {
        compose.runOnUiThread { compose.activity.setContent { DejaViewTheme(content) } }
    }
}
