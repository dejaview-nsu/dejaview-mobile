package ru.dejaview.audio.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

@Composable
internal fun AudioScreen(
    state: AudioState,
    model: AudioViewModel,
    modifier: Modifier,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var denied by rememberSaveable { mutableStateOf(false) }
    LifecycleResumeEffect(context) {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) denied = false
        onPauseOrDispose {}
    }
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
