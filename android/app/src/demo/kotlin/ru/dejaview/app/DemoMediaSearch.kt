package ru.dejaview.app

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import ru.dejaview.core.ui.components.SearchLoadingScreen
import ru.dejaview.model.MovieSummary
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode
import java.time.Instant
import java.util.UUID

@Composable
internal fun DemoMediaSearch(
    mode: SearchMode,
    onCompleted: (SearchCompletion) -> Unit,
    modifier: Modifier,
) {
    var count by remember { mutableStateOf<Int?>(null) }
    val input = stringResource(R.string.demo_search_fixture)
    val title = stringResource(R.string.demo_search_movie)
    LaunchedEffect(count) {
        val size = count ?: return@LaunchedEffect
        val requestId = UUID.randomUUID().toString()
        delay(650)
        if (count != size) return@LaunchedEffect
        val movies = List(size) { index -> MovieSummary((index + 1).toLong(), "$title ${index + 1}") }
        count = null
        onCompleted(SearchCompletion(requestId, mode, movies, input, Instant.now()))
    }
    if (count != null) {
        SearchLoadingScreen(onCancel = { count = null }, modifier = modifier)
    } else {
        Column(modifier) {
            listOf(10, 1, 0).forEach { size ->
                TextButton(onClick = { count = size }) {
                    Text(stringResource(R.string.demo_search_result_count, size))
                }
            }
        }
    }
}
