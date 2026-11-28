package ru.dejaview.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import ru.dejaview.core.ui.R
import ru.dejaview.core.ui.theme.DejaViewType

@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    close: Boolean = false,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(
                painterResource(if (close) R.drawable.dv_close else R.drawable.dv_back),
                stringResource(if (close) R.string.dv_close else R.string.dv_back),
            )
        }
        Text(title, Modifier.semantics { heading() }, style = DejaViewType.title)
    }
}
