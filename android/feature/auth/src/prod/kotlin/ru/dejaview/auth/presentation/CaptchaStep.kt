package ru.dejaview.auth.presentation

import androidx.compose.runtime.Composable

// Q-03: the trusted page and token return protocol must be supplied by the service.
internal const val CHALLENGE_AVAILABLE = false

@Suppress("UNUSED_PARAMETER")
@Composable
internal fun CaptchaStep(
    onCancel: () -> Unit,
    onToken: (String) -> Unit,
) = Unit
