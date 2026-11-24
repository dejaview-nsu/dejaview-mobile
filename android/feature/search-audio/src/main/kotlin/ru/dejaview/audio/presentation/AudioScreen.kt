package ru.dejaview.audio.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.dejaview.contracts.AppError
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.feature.searchaudio.R

@Composable
internal fun AudioScreen(
    state: AudioState,
    model: AudioViewModel,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var denied by remember { mutableStateOf(false) }
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            denied = !granted
            if (granted) model.record()
        }
    val document = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let(model::pick) }
    val ready = state.phase == AudioPhase.Ready
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(vertical = DejaViewSpacing.large),
        verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(DejaViewSpacing.medium), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(ru.dejaview.core.ui.R.drawable.dv_search_audio), null, Modifier.size(28.dp), tint = DejaViewColors.accent)
            Text(stringResource(R.string.audio_title), style = DejaViewType.heading)
        }
        Text(stringResource(R.string.audio_formats), style = DejaViewType.body, color = DejaViewColors.secondaryText)
        state.draft?.let {
            Surface(modifier = Modifier.fillMaxWidth(), shape = DejaViewShapes.card, color = DejaViewColors.surface) {
                Text(
                    stringResource(R.string.audio_draft, it.displayName, (it.durationMs ?: 0) / 1000.0, it.byteCount / 1024.0),
                    modifier = Modifier.padding(DejaViewSpacing.large),
                    style = DejaViewType.bodyMedium,
                )
            }
        }
        if (ready) {
            PrimaryButton(stringResource(R.string.audio_pick), {
                document.launch(arrayOf("audio/*", "application/ogg", "video/mp4"))
            }, Modifier.fillMaxWidth())
            TextButton(onClick = {
                if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    denied = false
                    model.record()
                } else {
                    permission.launch(Manifest.permission.RECORD_AUDIO)
                }
            }) { Text(stringResource(R.string.audio_record)) }
            if (denied) {
                Text(stringResource(R.string.audio_denied))
                if (activity?.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) == false) {
                    TextButton(onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + context.packageName),
                            ),
                        )
                    }) {
                        Text(stringResource(R.string.audio_settings))
                    }
                }
            }
            if (state.draft != null) {
                TextButton(onClick = model::play) { Text(stringResource(R.string.audio_play)) }
                TextButton(onClick = model::remove) { Text(stringResource(R.string.audio_delete)) }
                PrimaryButton(stringResource(R.string.audio_submit), model::submit, Modifier.fillMaxWidth())
            }
        } else {
            when (state.phase) {
                AudioPhase.Playing -> TextButton(onClick = model::interrupt) { Text(stringResource(R.string.audio_stop_play)) }
                AudioPhase.Recording -> PrimaryButton(stringResource(R.string.audio_stop_record), model::finishRecording)
                AudioPhase.Importing -> Text(stringResource(R.string.audio_import))
                AudioPhase.Uploading -> {
                    LinearProgressIndicator(
                        progress = { state.sent.toFloat() / (state.draft?.byteCount ?: 1) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(stringResource(R.string.audio_upload, state.sent, state.draft?.byteCount ?: 0))
                }
                AudioPhase.Processing -> {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text(stringResource(R.string.audio_processing))
                }
                AudioPhase.Ready -> Unit
            }
            if (state.phase in setOf(AudioPhase.Uploading, AudioPhase.Processing, AudioPhase.Importing)) {
                TextButton(onClick = model::cancelUpload) { Text(stringResource(R.string.audio_cancel)) }
            }
        }
        state.error?.let { Text(stringResource(audioError(it)), color = MaterialTheme.colorScheme.error) }
    }
}

private fun audioError(error: AppError): Int =
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
