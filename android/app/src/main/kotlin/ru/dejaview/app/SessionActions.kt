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
import ru.dejaview.model.SearchCompletion
import ru.dejaview.navigation.PendingAction
import ru.dejaview.navigation.PendingActionStore
import ru.dejaview.navigation.SearchRoute
import javax.inject.Inject

class SessionServices
    @Inject
    constructor(
        val reader: SessionReader,
        val controller: SessionController,
        val login: LoginService,
    )

internal class SessionActions(
    private val services: SessionServices,
) : ViewModel() {
    private val pending = PendingActionStore()
    private var result: Pair<String, SearchCompletion>? = null
    private val message = MutableStateFlow<Int?>(null)
    val notice = message.asStateFlow()

    fun expect(route: SearchRoute): String = pending.expect(PendingAction.OpenMode(route.mode, route.draftId))

    fun consume(id: String?): SearchRoute? {
        val action = pending.consume(id) as? PendingAction.OpenMode ?: return null
        return SearchRoute(action.mode, action.draftId)
    }

    fun completed(
        scope: String,
        completion: SearchCompletion,
    ) {
        result = scope to completion
    }

    fun result(
        scope: String?,
        id: String,
    ) = result?.takeIf { it.first == scope && it.second.requestId == id }?.second

    fun forgetPrivateData() {
        result = null
    }

    fun logout() {
        pending.clear()
        forgetPrivateData()
        viewModelScope.launch {
            if (services.controller.logout() is AppResult.Failure) message.value = R.string.logout_unconfirmed
        }
    }

    fun dismiss() {
        message.value = null
    }
}
