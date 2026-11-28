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
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var denied by remember { mutableStateOf(false) }
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            denied = !granted
            if (granted) model.record()
        }
    val document = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let(model::pick) }
    val onPick = {
        denied = false
        model.interrupt()
        document.launch(arrayOf("audio/*", "application/ogg", "video/mp4"))
    }
    val onRecord = {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            denied = false
            model.record()
        } else {
            permission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    val onSettings = {
        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.packageName)))
    }
    AudioSearchPage(state, model, denied, onRecord, onPick, onSettings, onClose, modifier)
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
