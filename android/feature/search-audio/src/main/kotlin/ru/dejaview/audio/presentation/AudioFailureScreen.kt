package ru.dejaview.audio.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.dejaview.contracts.AppError
import ru.dejaview.core.ui.components.FeedbackKind
import ru.dejaview.core.ui.components.SearchStatusPage
import ru.dejaview.feature.searchaudio.R

@Composable
internal fun AudioFailureScreen(
    state: AudioState,
    error: AppError,
    onRetry: () -> Unit,
    onPick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier,
) {
    val badFile = error is AppError.InvalidFile || error is AppError.Validation
    val kind =
        when {
            badFile -> FeedbackKind.InvalidFile
            error is AppError.Offline || error is AppError.Timeout -> FeedbackKind.Offline
            else -> FeedbackKind.Error
        }
    val replaceFile = badFile || state.draft == null
    SearchStatusPage(
        kind,
        stringResource(R.string.audio_title),
        onPrimary = if (replaceFile) onPick else onRetry,
        onSecondary = onClose,
        modifier = modifier,
        primaryEnabled = state.retryInSeconds == 0L,
        primary = stringResource(if (replaceFile) R.string.audio_pick else R.string.audio_retry),
        message = if (state.retryInSeconds > 0) stringResource(R.string.audio_retry_after, state.retryInSeconds)
            else stringResource(audioErrorMessage(error)),
    )
}

private fun audioErrorMessage(error: AppError): Int =
    when (error.code) {
        "FILE_TOO_LARGE" -> R.string.audio_large
        "AUDIO_TOO_LONG" -> R.string.audio_long
        "UNSUPPORTED_FORMAT" -> R.string.audio_format
        "MIC_BUSY" -> R.string.audio_mic_error
        "PLAYBACK" -> R.string.audio_play_error
        else ->
            when (error) {
                is AppError.Offline, is AppError.Timeout -> R.string.audio_offline
                is AppError.ServiceUnavailable -> R.string.audio_unavailable
                is AppError.Unauthenticated -> R.string.audio_expired
                is AppError.InvalidFile, is AppError.Validation -> R.string.audio_invalid
                else -> R.string.audio_failure
            }
    }
