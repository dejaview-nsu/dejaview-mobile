package ru.dejaview.auth.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.dejaview.auth.domain.LoginService
import ru.dejaview.contracts.AppError
import ru.dejaview.core.ui.components.DejaViewTextField
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.feature.auth.R

@Composable
fun LoginScreen(
    service: LoginService,
    onCancel: () -> Unit,
    onRegister: () -> Unit,
) {
    val model = viewModel<LoginViewModel> { LoginViewModel(service) }
    val state by model.state.collectAsStateWithLifecycle()
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var challenge by remember { mutableStateOf(false) }
    val cancel = {
        model.cancel()
        onCancel()
    }
    BackHandler(onBack = cancel)
    Column(
        Modifier.imePadding().verticalScroll(rememberScrollState()).padding(vertical = DejaViewSpacing.large),
        verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
    ) {
        Text(stringResource(R.string.auth_title), style = DejaViewType.heading)
        DejaViewTextField(
            login,
            {
                login = it
                model.changedIdentity()
            },
            stringResource(R.string.auth_login),
            enabled = !state.busy,
            isError = (state.error as? AppError.Validation)?.field == "login",
        )
        DejaViewTextField(
            password,
            { password = it },
            stringResource(R.string.auth_password),
            enabled = !state.busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = (state.error as? AppError.Validation)?.field == "password",
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { visible = !visible }) {
                    Text(stringResource(if (visible) R.string.auth_hide else R.string.auth_show))
                }
            },
        )
        state.error?.let { Text(stringResource(message(it)), color = MaterialTheme.colorScheme.error) }
        if (state.waitSeconds > 0) Text(stringResource(R.string.auth_wait, state.waitSeconds))
        if (state.captcha && !CHALLENGE_AVAILABLE) Text(stringResource(R.string.auth_challenge_unavailable))
        PrimaryButton(stringResource(if (state.busy) R.string.auth_busy else R.string.auth_submit), {
            if (state.captcha) challenge = true else model.submit(login, password, null)
        }, Modifier.fillMaxWidth(), enabled = !state.busy && state.waitSeconds == 0L && (!state.captcha || CHALLENGE_AVAILABLE))
        TextButton(onClick = cancel) { Text(stringResource(R.string.auth_cancel)) }
        TextButton(onClick = onRegister, enabled = !state.busy) { Text(stringResource(R.string.registration_submit)) }
    }
    if (challenge) {
        CaptchaStep(onCancel = { challenge = false }, onToken = { token ->
            challenge = false
            model.submit(login, password, token)
        })
    }
}

private fun message(error: AppError): Int =
    when (error.code) {
        "AUTH_ACCOUNT_BLOCKED" -> R.string.auth_blocked
        "AUTH_EMAIL_NOT_CONFIRMED" -> R.string.auth_unconfirmed
        "AUTH_INVALID_CREDENTIALS" -> R.string.auth_invalid
        "AUTH_CAPTCHA_REQUIRED", "AUTH_CAPTCHA_INVALID" -> R.string.auth_challenge
        "API_NOT_CONFIGURED" -> R.string.auth_config
        else ->
            when (error) {
                is AppError.Validation -> if (error.field == "password") R.string.auth_password_required else R.string.auth_bad_login
                is AppError.RateLimit -> R.string.auth_limited
                is AppError.Offline, is AppError.Timeout -> R.string.auth_offline
                else -> R.string.auth_failure
            }
    }
