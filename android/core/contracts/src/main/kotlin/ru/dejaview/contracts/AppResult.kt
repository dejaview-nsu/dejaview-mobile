package ru.dejaview.contracts

sealed interface AppResult<out T> {
    data class Success<T>(
        val value: T,
    ) : AppResult<T>

    data class Failure(
        val error: AppError,
    ) : AppResult<Nothing>
}

sealed interface AppError {
    val code: String?

    data class Validation(
        val field: String?,
        override val code: String? = null,
    ) : AppError

    data class InvalidFile(
        override val code: String? = null,
    ) : AppError

    data class Unauthenticated(
        override val code: String? = null,
    ) : AppError

    data class Forbidden(
        override val code: String? = null,
    ) : AppError

    data class NotFound(
        override val code: String? = null,
    ) : AppError

    data class RateLimit(
        val retryAfterSeconds: Long?,
        override val code: String? = null,
    ) : AppError

    data class Offline(
        override val code: String? = null,
    ) : AppError

    data class Timeout(
        override val code: String? = null,
    ) : AppError

    data class ServiceUnavailable(
        override val code: String? = null,
    ) : AppError

    data class Unknown(
        override val code: String? = null,
    ) : AppError
}
