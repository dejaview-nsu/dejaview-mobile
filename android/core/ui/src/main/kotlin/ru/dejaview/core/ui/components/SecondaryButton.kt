package ru.dejaview.core.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewType

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        enabled = enabled,
        shape = DejaViewShapes.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = DejaViewColors.elevated,
            contentColor = DejaViewColors.text,
            disabledContainerColor = DejaViewColors.surface,
            disabledContentColor = DejaViewColors.mutedText,
        ),
    ) {
        Text(text, style = DejaViewType.button)
    }
}
