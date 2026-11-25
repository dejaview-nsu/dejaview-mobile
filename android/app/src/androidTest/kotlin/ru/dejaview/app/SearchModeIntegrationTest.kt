package ru.dejaview.app

import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import ru.dejaview.contracts.session.SessionReader
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.core.ui.theme.DejaViewTheme
import ru.dejaview.model.MovieSummary
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode
import ru.dejaview.search.api.SearchModalityUi
import java.time.Instant
import java.util.Optional

class SearchModeIntegrationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun externalModeUsesSharedResultsAndDuplicateCompletionDoesNotReopenThem() {
        val session =
            object : SessionReader {
                override val state = MutableStateFlow<SessionState>(
                    SessionState.Authenticated("Демо", Instant.MAX, "integration-scope"),
                )
            }
        compose.runOnUiThread {
            val services = SearchServices(Optional.of(session), Optional.empty(), compose.activity.searchServices.defaults)
            compose.activity.setContent {
                DejaViewTheme {
                    AppNavHost(mapOf(SearchMode.VIDEO to VideoEntrypoint), services)
                }
            }
        }
        compose.onNodeWithText("Видео").performClick()
        compose.onNodeWithText("Завершить внешний поиск").performClick()
        compose.onNodeWithText("Синтетический фильм").assertIsDisplayed()
        compose.onNodeWithText("Это не тот фильм").performClick()
        compose.onNodeWithText("Завершить внешний поиск").performClick()
        compose.onNodeWithText("Завершить внешний поиск").assertIsDisplayed()
    }

    private object VideoEntrypoint : SearchModalityUi {
        override val mode = SearchMode.VIDEO

        @Composable
        override fun Content(
            draftId: String?,
            onCompleted: (SearchCompletion) -> Unit,
            modifier: Modifier,
        ) {
            val completion = SearchCompletion(
                "external-video-result",
                mode,
                listOf(MovieSummary(1, "Синтетический фильм")),
                "Синтетический запрос",
                Instant.EPOCH,
            )
            TextButton(modifier = modifier, onClick = {
                repeat(2) { onCompleted(completion) }
            }) {
                Text("Завершить внешний поиск")
            }
        }
    }
}
