package ru.dejaview.auth.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import ru.dejaview.core.ui.components.DejaViewTextField
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.feature.auth.R
import java.time.Year

@Composable
internal fun RegistrationFields(state: RegistrationState, onSubmit: (String, String, Int, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    val enabled = !state.busy && state.waitSeconds == 0L
    val year = birthYear.toIntOrNull()
    val yearValid = year != null && year in 1900..Year.now().value
    val mismatched = repeatPassword.isNotEmpty() && password != repeatPassword
    Text(stringResource(R.string.registration_intro), color = DejaViewColors.secondaryText, style = DejaViewType.body)
    DejaViewTextField(username, { username = it }, stringResource(R.string.registration_username), enabled = enabled)
    DejaViewTextField(
        email, { email = it }, stringResource(R.string.registration_email), enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    )
    DejaViewTextField(
        birthYear, { birthYear = it.filter(Char::isDigit).take(4) }, stringResource(R.string.registration_year),
        enabled = enabled, isError = birthYear.length == 4 && !yearValid,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
    DejaViewTextField(
        password, { password = it }, stringResource(R.string.auth_password), enabled = enabled,
        visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )
    DejaViewTextField(
        repeatPassword, { repeatPassword = it }, stringResource(R.string.registration_repeat), enabled = enabled,
        isError = mismatched, visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )
    if (mismatched) Text(stringResource(R.string.registration_mismatch), color = DejaViewColors.error)
    Text(stringResource(R.string.registration_password_hint), color = DejaViewColors.mutedText, style = DejaViewType.caption)
    RegistrationFeedback(state)
    PrimaryButton(
        stringResource(R.string.registration_submit),
        { if (year != null) onSubmit(username, email, year, password) },
        Modifier.fillMaxWidth(),
        enabled = enabled && yearValid && username.isNotBlank() && email.isNotBlank() && password.isNotEmpty() && password == repeatPassword,
    )
}
