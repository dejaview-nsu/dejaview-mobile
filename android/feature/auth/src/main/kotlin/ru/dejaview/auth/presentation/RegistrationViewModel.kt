package ru.dejaview.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.dejaview.auth.domain.RegistrationService
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult

internal data class RegistrationState(
    val confirmedAddress: String? = null,
    val busy: Boolean = false,
    val waitSeconds: Long = 0,
    val error: AppError? = null,
)

internal class RegistrationViewModel(
    private val service: RegistrationService,
) : ViewModel() {
    private val mutableState = MutableStateFlow(RegistrationState())
    val state = mutableState.asStateFlow()

    fun register(
        username: String,
        email: String,
        birthYear: Int,
        password: String,
    ) {
        val address = email.trim()
        submit(address) { service.register(username.trim(), address, birthYear, password) }
    }

    fun resend() {
        val address = mutableState.value.confirmedAddress ?: return
        submit(address) { service.resend(address) }
    }

    private fun submit(
        address: String,
        request: suspend () -> AppResult<Int>,
    ) {
        val initial = mutableState.value
        if (initial.busy || initial.waitSeconds > 0) return
        mutableState.value = initial.copy(busy = true, error = null)
        viewModelScope.launch {
            val updated =
                when (val result = request()) {
                    is AppResult.Success -> RegistrationState(confirmedAddress = address, waitSeconds = result.value.toLong())
                    is AppResult.Failure ->
                        initial.copy(
                            error = result.error,
                            waitSeconds = (result.error as? AppError.RateLimit)?.retryAfterSeconds ?: 0,
                        )
                }
            mutableState.value = updated
            while (mutableState.value.waitSeconds > 0) {
                delay(1000)
                mutableState.update { it.copy(waitSeconds = it.waitSeconds - 1) }
            }
        }
    }
}
