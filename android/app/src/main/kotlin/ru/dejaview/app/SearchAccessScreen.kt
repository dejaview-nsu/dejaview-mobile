package ru.dejaview.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.components.SecondaryButton
import ru.dejaview.core.ui.components.icon
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.model.SearchMode

@Composable
internal fun SearchAccessScreen(mode: SearchMode, onLogin: () -> Unit, onRegister: () -> Unit, onTextSearch: () -> Unit) {
    val title =
        when (mode) {
            SearchMode.IMAGE -> R.string.search_access_image
            SearchMode.VIDEO -> R.string.search_access_video
            SearchMode.AUDIO -> R.string.search_access_audio
            SearchMode.TEXT -> R.string.search_login_required
        }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Surface(Modifier.fillMaxWidth(), shape = DejaViewShapes.sheet, color = DejaViewColors.surface) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(DejaViewSpacing.extraLarge),
                verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
            ) {
                Icon(painterResource(mode.icon), null, Modifier.size(34.dp), tint = DejaViewColors.accent)
                Text(stringResource(title), style = DejaViewType.heading)
                Text(stringResource(R.string.search_access_detail), color = DejaViewColors.secondaryText, style = DejaViewType.body)
                PrimaryButton(stringResource(R.string.search_login), onLogin, Modifier.fillMaxWidth())
                SecondaryButton(stringResource(R.string.search_access_register), onRegister, Modifier.fillMaxWidth())
                TextButton(onClick = onTextSearch, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.search_access_guest))
                }
            }
        }
    }
}
