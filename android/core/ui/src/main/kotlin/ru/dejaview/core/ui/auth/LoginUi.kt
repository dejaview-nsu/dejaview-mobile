package ru.dejaview.core.ui.auth

import androidx.compose.runtime.Composable

/** Login updates the shared session. Navigation stays with the caller. */
interface LoginUi {
    @Composable
    fun Content(onCancel: () -> Unit, onRegister: () -> Unit)

    @Composable
    fun Registration(onReturnToLogin: () -> Unit)
}
