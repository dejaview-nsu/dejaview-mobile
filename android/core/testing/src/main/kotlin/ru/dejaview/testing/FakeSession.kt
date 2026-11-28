package ru.dejaview.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.dejaview.contracts.AppResult
import ru.dejaview.contracts.session.SessionController
import ru.dejaview.contracts.session.SessionReader
import ru.dejaview.contracts.session.SessionState

class FakeSession(
    initial: SessionState = SessionState.Guest,
) : SessionReader,
    SessionController {
    private val mutable = MutableStateFlow(initial)
    override val state = mutable.asStateFlow()

    fun setState(value: SessionState) {
        mutable.value = value
    }

    fun expire() {
        mutable.value = SessionState.Expired
    }

    override suspend fun restore(): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun logout(): AppResult<Unit> {
        mutable.value = SessionState.Guest
        return AppResult.Success(Unit)
    }
}
