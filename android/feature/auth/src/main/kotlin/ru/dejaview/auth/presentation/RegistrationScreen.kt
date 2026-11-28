package ru.dejaview.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.dejaview.auth.domain.RegistrationService
import ru.dejaview.contracts.AppError
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.components.ScreenHeader
import ru.dejaview.core.ui.components.SecondaryButton
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.feature.auth.R

@Composable
internal fun RegistrationScreen(service: RegistrationService, onReturnToLogin: () -> Unit) {
    val model = viewModel { RegistrationViewModel(service) }
    val state by model.state.collectAsStateWithLifecycle()
    Column(Modifier.imePadding().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large)) {
        val address = state.confirmedAddress
        ScreenHeader(stringResource(if (address == null) R.string.registration_title else R.string.registration_email_title), onReturnToLogin)
        if (address == null) {
            RegistrationFields(state, model::register)
        } else {
            EmailConfirmation(address, state, model::resend, onReturnToLogin)
        }
    }
}

@Composable
private fun EmailConfirmation(address: String, state: RegistrationState, onResend: () -> Unit, onLogin: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = DejaViewSpacing.section),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
    ) {
        Text(stringResource(R.string.registration_check_mail), style = DejaViewType.heading, textAlign = TextAlign.Center)
        Text(stringResource(R.string.registration_email_sent, address), color = DejaViewColors.secondaryText, textAlign = TextAlign.Center)
        RegistrationFeedback(state)
        PrimaryButton(stringResource(R.string.auth_submit), onLogin, Modifier.fillMaxWidth())
        SecondaryButton(stringResource(R.string.registration_resend), onResend, Modifier.fillMaxWidth(), !state.busy && state.waitSeconds == 0L)
    }
}

@Composable
internal fun RegistrationFeedback(state: RegistrationState) {
    state.error?.let {
        val message =
            when {
                it is AppError.Offline || it is AppError.Timeout -> R.string.auth_offline
                it is AppError.RateLimit -> R.string.auth_limited
                it is AppError.Validation -> R.string.registration_invalid
                it.code == "AUTH_EMAIL_TAKEN" -> R.string.registration_email_taken
                it.code == "AUTH_USERNAME_TAKEN" -> R.string.registration_name_taken
                else -> R.string.registration_failure
            }
        Text(stringResource(message), color = DejaViewColors.error)
    }
    if (state.waitSeconds > 0) {
        Text(stringResource(R.string.auth_wait, state.waitSeconds), color = DejaViewColors.secondaryText)
    }
}
