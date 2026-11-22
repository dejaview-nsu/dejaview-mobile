package ru.dejaview.core.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
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

private const val PHONE_WIDTH_DP = 412
private const val NARROW_WIDTH_DP = 280
private const val LARGE_FONT_SCALE = 2f

@Preview(widthDp = PHONE_WIDTH_DP)
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

@Preview(widthDp = PHONE_WIDTH_DP)
@Preview(name = "Large text", widthDp = NARROW_WIDTH_DP, fontScale = LARGE_FONT_SCALE)
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

@Preview(widthDp = PHONE_WIDTH_DP)
@Composable
private fun FeedbackPreview(
    @PreviewParameter(FeedbackPreviewProvider::class) kind: FeedbackKind,
) = PreviewSurface {
    ContentFeedback(kind, onAction = {})
}

@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
    DejaViewTheme {
        Surface(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(DejaViewSpacing.screen),
                verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.extraLarge),
            ) {
                content()
            }
        }
    }
}
