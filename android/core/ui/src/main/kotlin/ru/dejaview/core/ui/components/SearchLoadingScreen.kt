package ru.dejaview.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.dejaview.core.ui.theme.DejaViewSpacing

@Composable
fun SearchLoadingScreen(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ContentFeedback(
            FeedbackKind.Loading,
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(vertical = DejaViewSpacing.large),
            onAction = onCancel,
        )
    }
}
