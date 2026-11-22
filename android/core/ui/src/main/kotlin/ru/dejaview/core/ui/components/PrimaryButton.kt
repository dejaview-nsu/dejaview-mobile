package ru.dejaview.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType

private val BUTTON_MIN_HEIGHT = 52.dp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = BUTTON_MIN_HEIGHT),
        enabled = enabled,
        shape = DejaViewShapes.pill,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = DejaViewColors.accent,
                contentColor = DejaViewColors.onAccent,
                disabledContainerColor = DejaViewColors.elevated,
                disabledContentColor = DejaViewColors.mutedText,
            ),
        contentPadding = PaddingValues(horizontal = DejaViewSpacing.extraLarge, vertical = DejaViewSpacing.medium),
    ) {
        Text(text, style = DejaViewType.button)
    }
}
