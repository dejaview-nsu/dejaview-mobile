package ru.dejaview.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ru.dejaview.core.ui.components.FeedbackKind

internal class FeedbackPreviewProvider : PreviewParameterProvider<FeedbackKind> {
    override val values = FeedbackKind.entries.asSequence()
}
