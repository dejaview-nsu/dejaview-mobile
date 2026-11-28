package ru.dejaview.app

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.dejaview.core.ui.components.ContentFeedback
import ru.dejaview.core.ui.components.FeedbackKind
import ru.dejaview.core.ui.components.ScreenHeader
import ru.dejaview.core.ui.components.ScreenLayout
import ru.dejaview.core.ui.components.SecondaryButton
import ru.dejaview.core.ui.components.label
import ru.dejaview.model.SearchCompletion
import ru.dejaview.navigation.SearchRoute

@Suppress("UNUSED_PARAMETER")
@Composable
internal fun SearchFallback(
    route: SearchRoute,
    onCompleted: (SearchCompletion) -> Unit,
    modifier: Modifier,
) {
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val onBack = { dispatcher?.onBackPressed(); Unit }
    ScreenLayout(
        modifier,
        header = { ScreenHeader(stringResource(route.mode.label), onBack, close = true) },
        actions = { SecondaryButton(stringResource(R.string.search_fallback_home), onBack, Modifier.fillMaxWidth()) },
    ) {
        ContentFeedback(
            FeedbackKind.Error,
            title = stringResource(R.string.search_mode_unavailable),
            message = stringResource(R.string.search_fallback_detail),
        )
    }
}
