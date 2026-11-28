package ru.dejaview.auth.presentation

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.dejaview.auth.domain.LoginResult
import ru.dejaview.auth.domain.LoginService
import ru.dejaview.auth.domain.validLogin
import ru.dejaview.contracts.AppError

internal data class LoginState(
    val busy: Boolean = false,
    val error: AppError? = null,
    val captcha: Boolean = false,
    val waitSeconds: Long = 0,
    val complete: Boolean = false,
)

internal class LoginViewModel(
    private val service: LoginService,
    private val elapsedMillis: () -> Long = SystemClock::elapsedRealtime,
) : ViewModel() {
    private val mutable = MutableStateFlow(LoginState())
    val state = mutable.asStateFlow()
    private var submission: Job? = null

    fun cancel() {
        submission?.cancel()
    }

    fun submit(
        name: String,
        password: String,
        token: String?,
    ) {
        val previous = mutable.value
        if (previous.busy || previous.complete || previous.waitSeconds > 0 || (previous.captcha && token == null)) return
        val login = name.trim()
        val field =
            when {
                !validLogin(login) -> "login"
                password.isEmpty() -> "password"
                else -> null
            }
        if (field != null) {
            mutable.value = previous.copy(error = AppError.Validation(field))
            return
        }
        mutable.value = previous.copy(busy = true, error = null)
        submission =
            viewModelScope.launch {
                when (val result = service.login(login, password, token)) {
                    LoginResult.Success -> mutable.value = LoginState(complete = true)
                    is LoginResult.Rejected -> {
                        val seconds = (result.error as? AppError.RateLimit)?.retryAfterSeconds ?: 0L
                        mutable.value = LoginState(error = result.error, captcha = result.requiresCaptcha, waitSeconds = seconds)
                        if (seconds > 0) {
                            val started = elapsedMillis()
                            while (mutable.value.waitSeconds > 0) {
                                delay(1000)
                                mutable.value =
                                    mutable.value.copy(
                                        waitSeconds = (seconds - (elapsedMillis() - started) / 1000).coerceAtLeast(0),
                                    )
                            }
                        }
                    }
                }
            }
    }

    fun changedIdentity() {
        if (!mutable.value.busy && mutable.value.waitSeconds == 0L) mutable.value = LoginState()
    }
}
