package ru.dejaview.auth.domain

import ru.dejaview.contracts.AppError

sealed interface LoginResult {
    data object Success : LoginResult

    data class Rejected(
        val error: AppError,
        val requiresCaptcha: Boolean = false,
    ) : LoginResult
}

interface LoginService {
    suspend fun login(
        login: String,
        password: String,
        captchaToken: String?,
    ): LoginResult
}

private val username = Regex("[A-Za-z0-9_]{3,30}")
private val emailLocal = Regex("[A-Za-z0-9!#$%&'*+/=?^_`{|}~.\\-]{1,64}")
private val emailDomain = Regex("[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+")

internal fun validLogin(value: String): Boolean {
    if ('@' !in value) return username.matches(value)
    val local = value.substringBefore('@')
    return value.length <= 254 && emailLocal.matches(local) && !local.startsWith('.') &&
        !local.endsWith('.') && ".." !in local && emailDomain.matches(value.substringAfter('@'))
}
