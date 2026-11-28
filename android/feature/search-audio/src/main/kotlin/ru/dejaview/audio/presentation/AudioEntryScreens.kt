package ru.dejaview.audio.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.components.FeedbackKind
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.components.ScreenHeader
import ru.dejaview.core.ui.components.ScreenLayout
import ru.dejaview.core.ui.components.SearchStatusPage
import ru.dejaview.core.ui.components.SecondaryButton
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.feature.searchaudio.R

@Composable
internal fun AudioEntryScreen(
    onRecord: () -> Unit,
    onPick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier,
) {
    val header = stringResource(R.string.audio_title)
    ScreenLayout(
        modifier,
        header = { ScreenHeader(header, onClose, close = true) },
        actions = {
            PrimaryButton(stringResource(R.string.audio_record), onRecord, Modifier.fillMaxWidth())
            SecondaryButton(stringResource(R.string.audio_pick), onPick, Modifier.fillMaxWidth())
            Text(
                stringResource(R.string.audio_formats),
                Modifier.fillMaxWidth(),
                color = DejaViewColors.mutedText,
                style = DejaViewType.caption,
                textAlign = TextAlign.Center,
            )
        },
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
        ) {
            Box(Modifier.size(112.dp).background(DejaViewColors.surface, DejaViewShapes.pill), contentAlignment = Alignment.Center) {
                Icon(
                    painterResource(ru.dejaview.core.ui.R.drawable.dv_search_audio),
                    null,
                    Modifier.size(44.dp),
                    tint = DejaViewColors.accent,
                )
            }
            Text(stringResource(R.string.audio_listening_intro), style = DejaViewType.heading, textAlign = TextAlign.Center)
            Text(stringResource(R.string.audio_recording_tip), color = DejaViewColors.secondaryText, textAlign = TextAlign.Center)
        }
    }
}

@Composable
internal fun AudioPermissionScreen(
    onSettings: () -> Unit,
    onPick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier,
) {
    SearchStatusPage(
        FeedbackKind.Microphone,
        stringResource(R.string.audio_title),
        onPrimary = onSettings,
        onSecondary = onPick,
        modifier = modifier,
        onBack = onClose,
        secondary = stringResource(R.string.audio_pick),
    )
}
