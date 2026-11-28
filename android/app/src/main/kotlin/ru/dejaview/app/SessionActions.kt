package ru.dejaview.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.dejaview.auth.domain.LoginService
import ru.dejaview.contracts.AppResult
import ru.dejaview.contracts.session.SessionController
import ru.dejaview.contracts.session.SessionReader
import javax.inject.Inject

class SessionServices
    @Inject
    constructor(
        val reader: SessionReader,
        val controller: SessionController,
        val login: LoginService,
    )

internal class SessionActions(
    private val controller: SessionController,
) : ViewModel() {
    private val message = MutableStateFlow<Int?>(null)
    val notice = message.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            if (controller.logout() is AppResult.Failure) message.value = R.string.logout_unconfirmed
        }
    }

    fun dismiss() {
        message.value = null
    }
}
