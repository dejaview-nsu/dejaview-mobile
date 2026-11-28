package ru.dejaview.audio.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.dejaview.core.ui.components.SearchLoadingScreen
import ru.dejaview.feature.searchaudio.R

@Composable
internal fun AudioSearchPage(
    state: AudioState,
    model: AudioViewModel,
    denied: Boolean,
    onRecord: () -> Unit,
    onPick: () -> Unit,
    onSettings: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier,
) {
    val error = state.error
    when {
        state.phase == AudioPhase.Recording -> AudioRecordingScreen(state, model::finishRecording, onPick, onClose, modifier)
        state.phase == AudioPhase.Importing || state.phase == AudioPhase.Uploading || state.phase == AudioPhase.Processing ->
            AudioProgress(state, model::cancelUpload, modifier)
        denied -> AudioPermissionScreen(onSettings, onPick, onClose, modifier)
        error != null -> AudioFailureScreen(state, error, model::submit, onPick, onClose, modifier)
        state.draft != null -> AudioPreviewScreen(state, model, onRecord, onPick, onClose, modifier)
        else -> AudioEntryScreen(onRecord, onPick, onClose, modifier)
    }
}

@Composable
private fun AudioProgress(state: AudioState, onCancel: () -> Unit, modifier: Modifier) {
    val bytes = state.draft?.byteCount
    val uploading = state.phase == AudioPhase.Uploading && bytes != null && bytes > 0
    val title =
        when (state.phase) {
            AudioPhase.Importing -> R.string.audio_import
            AudioPhase.Uploading -> R.string.audio_sending
            else -> R.string.audio_processing
        }
    SearchLoadingScreen(
        onCancel,
        modifier,
        title = stringResource(title),
        progress = if (uploading && bytes != null) (state.sent.toFloat() / bytes).coerceIn(0f, 1f) else null,
        detail = if (uploading) stringResource(R.string.audio_upload, state.sent, bytes ?: 0) else null,
        header = stringResource(R.string.audio_title),
    )
}
