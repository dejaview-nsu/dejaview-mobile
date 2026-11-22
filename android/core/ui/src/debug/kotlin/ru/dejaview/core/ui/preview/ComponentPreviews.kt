package ru.dejaview.core.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.dejaview.core.ui.R
import ru.dejaview.core.ui.components.ContentFeedback
import ru.dejaview.core.ui.components.DejaViewTextField
import ru.dejaview.core.ui.components.FeedbackKind
import ru.dejaview.core.ui.components.MovieCard
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.components.SearchModeSelector
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewTheme
import ru.dejaview.model.MovieSummary
import ru.dejaview.model.SearchMode

@Preview(name = "Controls · 412 dp", widthDp = 412, heightDp = 700, locale = "ru")
@Composable
private fun ControlsPreview() =
    PreviewSurface {
        val submit = stringResource(R.string.dv_preview_submit)
        PrimaryButton(submit, onClick = {})
        PrimaryButton(submit, onClick = {}, enabled = false)
        DejaViewTextField(
            value = "",
            onValueChange = {},
            label = stringResource(R.string.dv_preview_field),
            placeholder = stringResource(R.string.dv_preview_placeholder),
        )
        SearchModeSelector(selectedMode = SearchMode.TEXT, onModeSelected = {})
    }

@Preview(name = "Movie · 412 dp", widthDp = 412, locale = "ru")
@Preview(name = "Movie · 280 dp · 200%", widthDp = 280, fontScale = 2f, locale = "ru")
@Composable
private fun MoviePreview() =
    PreviewSurface {
        MovieCard(
            movie =
                MovieSummary(
                    movieId = 1,
                    title = stringResource(R.string.dv_preview_movie),
                    year = 2024,
                    overview = stringResource(R.string.dv_preview_plot),
                ),
            highlighted = true,
        )
    }

@Preview(name = "Feedback · 412 dp", widthDp = 412, heightDp = 1450, locale = "ru")
@Composable
private fun FeedbackPreview() =
    PreviewSurface {
        for (kind in FeedbackKind.entries) {
            ContentFeedback(kind, onAction = {})
        }
    }

@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
    DejaViewTheme {
        Surface(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(DejaViewSpacing.screen),
                verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.extraLarge),
            ) {
                content()
            }
        }
    }
}
