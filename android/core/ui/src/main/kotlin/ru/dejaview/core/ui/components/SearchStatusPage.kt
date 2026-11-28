package ru.dejaview.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.dejaview.core.ui.R

@Composable
fun SearchStatusPage(
    kind: FeedbackKind,
    header: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = onSecondary,
    primaryEnabled: Boolean = true,
    title: String = stringResource(kind.title),
    message: String = stringResource(kind.message),
    primary: String = stringResource(kind.action),
    secondary: String = stringResource(R.string.dv_search_home),
    details: (@Composable () -> Unit)? = null,
) {
    ScreenLayout(
        modifier,
        header = { ScreenHeader(header, onBack, close = kind != FeedbackKind.Empty) },
        actions = {
            PrimaryButton(primary, onPrimary, Modifier.fillMaxWidth(), enabled = primaryEnabled)
            SecondaryButton(secondary, onSecondary, Modifier.fillMaxWidth())
        },
    ) {
        ContentFeedback(kind, title = title, message = message)
        details?.invoke()
    }
}
