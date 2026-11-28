package ru.dejaview.audio.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.components.ScreenHeader
import ru.dejaview.core.ui.components.ScreenLayout
import ru.dejaview.core.ui.components.SecondaryButton
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.feature.searchaudio.R

@Composable
internal fun AudioPreviewScreen(
    state: AudioState,
    model: AudioViewModel,
    onRecord: () -> Unit,
    onPick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val draft = state.draft ?: return
    val playing = state.phase == AudioPhase.Playing
    ScreenLayout(
        modifier,
        header = { ScreenHeader(stringResource(R.string.audio_title), onBack, close = true) },
        contentAlignment = Alignment.TopCenter,
        actions = {
            PrimaryButton(stringResource(R.string.audio_submit), model::submit, Modifier.fillMaxWidth(), enabled = !playing)
            SecondaryButton(stringResource(R.string.audio_again), onRecord, Modifier.fillMaxWidth(), enabled = !playing)
        },
    ) {
        Column(
            Modifier.fillMaxWidth().padding(top = DejaViewSpacing.doubleLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
        ) {
            Text(stringResource(R.string.audio_preview_ready), color = DejaViewColors.accent, style = DejaViewType.overline)
            Text(stringResource(R.string.audio_preview_title), style = DejaViewType.heading, textAlign = TextAlign.Center)
            Text(stringResource(R.string.audio_preview_tip), color = DejaViewColors.secondaryText, textAlign = TextAlign.Center)
            Surface(Modifier.fillMaxWidth(), shape = DejaViewShapes.card, color = DejaViewColors.surface) {
                Column(Modifier.padding(DejaViewSpacing.large)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { if (playing) model.interrupt() else model.play() }) {
                            Text(stringResource(if (playing) R.string.audio_stop_play else R.string.audio_play))
                        }
                        val duration = draft.durationMs?.coerceAtLeast(1) ?: 1
                        AudioWaveform(state.waveform, state.elapsedMs.toFloat() / duration, Modifier.weight(1f))
                    }
                    Text(
                        stringResource(R.string.audio_draft, draft.displayName, (draft.durationMs ?: 0) / 1000.0, draft.byteCount / 1024.0),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = DejaViewType.caption,
                        color = DejaViewColors.secondaryText,
                    )
                }
            }
            TextButton(onClick = onPick, enabled = !playing) { Text(stringResource(R.string.audio_pick)) }
        }
    }
}
