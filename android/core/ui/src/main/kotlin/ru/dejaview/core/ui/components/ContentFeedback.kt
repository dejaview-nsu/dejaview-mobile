package ru.dejaview.core.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.R
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType

private val FEEDBACK_BADGE_SIZE = 80.dp
private val FEEDBACK_ICON_SIZE = 36.dp

enum class FeedbackKind(
    @get:StringRes internal val title: Int,
    @get:StringRes internal val message: Int,
    @get:StringRes internal val action: Int,
    @get:DrawableRes internal val icon: Int?,
) {
    Loading(R.string.dv_loading, R.string.dv_loading_message, R.string.dv_cancel, null),
    Empty(R.string.dv_empty, R.string.dv_empty_message, R.string.dv_edit_description, R.drawable.dv_state_empty),
    Error(R.string.dv_error, R.string.dv_error_message, R.string.dv_retry, R.drawable.dv_state_error),
    Offline(R.string.dv_offline, R.string.dv_offline_message, R.string.dv_retry, R.drawable.dv_state_offline),
    InvalidFile(R.string.dv_file_error, R.string.dv_file_error_message, R.string.dv_choose_file, R.drawable.dv_state_error),
    Microphone(R.string.dv_microphone, R.string.dv_microphone_message, R.string.dv_settings, R.drawable.dv_state_microphone),
}

@Composable
fun ContentFeedback(
    kind: FeedbackKind,
    modifier: Modifier = Modifier,
    title: String = stringResource(kind.title),
    message: String = stringResource(kind.message),
    actionText: String = stringResource(kind.action),
    onAction: (() -> Unit)? = null,
    secondaryAction: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
    ) {
        FeedbackIllustration(kind)
        Text(
            text = title,
            modifier =
                Modifier.semantics {
                    heading()
                    liveRegion = LiveRegionMode.Polite
                },
            style = DejaViewType.heading,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = DejaViewType.body,
            color = DejaViewColors.secondaryText,
            textAlign = TextAlign.Center,
        )
        if (onAction != null && kind == FeedbackKind.Loading) {
            TextButton(onClick = onAction) { Text(actionText) }
        } else if (onAction != null) {
            PrimaryButton(actionText, onAction, Modifier.fillMaxWidth())
        }
        secondaryAction?.invoke()
    }
}

@Composable
private fun FeedbackIllustration(kind: FeedbackKind) {
    val icon = kind.icon
    if (icon == null) {
        CircularProgressIndicator(color = DejaViewColors.accent)
        return
    }

    val tint = if (kind == FeedbackKind.InvalidFile || kind == FeedbackKind.Microphone) DejaViewColors.error else DejaViewColors.accent
    Surface(
        modifier = Modifier.size(FEEDBACK_BADGE_SIZE),
        shape = DejaViewShapes.pill,
        color = DejaViewColors.surface,
        border = BorderStroke(DejaViewShapes.outlineWidth, DejaViewColors.strongBorder),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(FEEDBACK_ICON_SIZE),
                tint = tint,
            )
        }
    }
}
