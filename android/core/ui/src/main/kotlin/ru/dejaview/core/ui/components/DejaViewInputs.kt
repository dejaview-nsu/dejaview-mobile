package ru.dejaview.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
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

@Composable
fun DejaViewTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = minLines,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Column(modifier) {
        Text(
            text = label,
            modifier = Modifier.clearAndSetSemantics {}.padding(bottom = DejaViewSpacing.small),
            color = DejaViewColors.secondaryText,
            style = DejaViewType.bodySmallMedium,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            enabled = enabled,
            textStyle = DejaViewType.body,
            placeholder = placeholder?.let { hint -> { Text(hint, style = DejaViewType.body) } },
            supportingText = supportingText,
            isError = isError,
            singleLine = maxLines == 1,
            minLines = minLines,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            shape = DejaViewShapes.field,
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DejaViewColors.surface,
                    unfocusedContainerColor = DejaViewColors.surface,
                    disabledContainerColor = DejaViewColors.surface,
                    errorContainerColor = DejaViewColors.surface,
                    focusedBorderColor = DejaViewColors.accent,
                    unfocusedBorderColor = DejaViewColors.border,
                    errorBorderColor = DejaViewColors.error,
                    focusedPlaceholderColor = DejaViewColors.mutedText,
                    unfocusedPlaceholderColor = DejaViewColors.mutedText,
                ),
        )
    }
}
