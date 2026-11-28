package ru.dejaview.audio.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
internal fun AudioRecordingScreen(
    state: AudioState,
    onStop: () -> Unit,
    onPick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val stop = stringResource(R.string.audio_stop_record)
    ScreenLayout(
        modifier,
        header = { ScreenHeader(stringResource(R.string.audio_title), onBack, close = true) },
        actions = {
            PrimaryButton(stop, onStop, Modifier.fillMaxWidth())
            SecondaryButton(stringResource(R.string.audio_pick), onPick, Modifier.fillMaxWidth())
        },
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
        ) {
            Text(stringResource(R.string.audio_recording_state), color = DejaViewColors.recording, style = DejaViewType.overline)
            Text(stringResource(R.string.audio_listening), style = DejaViewType.heading)
            Text(
                stringResource(R.string.audio_recording_tip), color = DejaViewColors.secondaryText,
                textAlign = TextAlign.Center, style = DejaViewType.body,
            )
            Box(
                Modifier.size(168.dp).border(1.dp, DejaViewColors.recording.copy(alpha = .38f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = onStop,
                    modifier = Modifier.size(108.dp).background(DejaViewColors.recording, CircleShape)
                        .semantics { contentDescription = stop },
                ) {
                    Box(Modifier.size(34.dp).background(DejaViewColors.background, DejaViewShapes.poster))
                }
            }
            Text(
                stringResource(R.string.audio_recording_time, (state.elapsedMs / 1000).coerceAtMost(10)),
                style = DejaViewType.headline,
            )
            AudioWaveform(state.recordingLevels)
        }
    }
}
