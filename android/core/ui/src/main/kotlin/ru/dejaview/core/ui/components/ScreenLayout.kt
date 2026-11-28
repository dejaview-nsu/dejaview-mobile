package ru.dejaview.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.dejaview.core.ui.theme.DejaViewSpacing

@Composable
fun ScreenLayout(
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        header?.invoke()
        Box(
            Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = contentAlignment,
        ) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = DejaViewSpacing.large),
                verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
                content = { content() },
            )
        }
        if (actions != null) {
            Column(
                Modifier.fillMaxWidth().padding(bottom = DejaViewSpacing.extraLarge),
                verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.medium),
            ) {
                actions()
            }
        }
    }
}
