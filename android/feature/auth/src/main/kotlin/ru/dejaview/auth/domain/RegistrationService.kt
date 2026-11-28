package ru.dejaview.auth.domain

import ru.dejaview.contracts.AppResult

internal interface RegistrationService {
    suspend fun register(
        username: String,
        email: String,
        birthYear: Int,
        password: String,
    ): AppResult<Int>

    suspend fun resend(email: String): AppResult<Int>
}
