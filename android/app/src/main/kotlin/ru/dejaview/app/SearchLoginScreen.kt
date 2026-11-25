package ru.dejaview.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType

@Composable
internal fun SearchLoginScreen(
    onCancel: () -> Unit,
    onDemoLogin: (() -> Unit)?,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = DejaViewSpacing.small),
            shape = DejaViewShapes.card,
            color = DejaViewColors.surface,
        ) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(DejaViewSpacing.large),
                verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
            ) {
                Text(
                    stringResource(R.string.search_login_required),
                    modifier = Modifier.semantics { heading() },
                    style = DejaViewType.heading,
                )
                Text(
                    stringResource(
                        if (onDemoLogin == null) R.string.search_login_unavailable else R.string.search_login_message,
                    ),
                )
                if (onDemoLogin != null) {
                    PrimaryButton(stringResource(R.string.search_demo_login), onDemoLogin, Modifier.fillMaxWidth())
                }
                TextButton(onClick = onCancel) { Text(stringResource(R.string.search_login_cancel)) }
            }
        }
    }
}
