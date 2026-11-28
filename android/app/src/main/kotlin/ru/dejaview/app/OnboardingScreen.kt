package ru.dejaview.app

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.dejaview.core.ui.components.PrimaryButton
import ru.dejaview.core.ui.theme.DejaViewColors
import ru.dejaview.core.ui.theme.DejaViewShapes
import ru.dejaview.core.ui.theme.DejaViewSpacing
import ru.dejaview.core.ui.theme.DejaViewType
import ru.dejaview.core.ui.R as UiR

@Composable
internal fun OnboardingScreen(onContinue: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).heightIn(min = maxHeight).padding(vertical = DejaViewSpacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.large),
        ) {
            OnboardingArtwork()
            Text(stringResource(R.string.onboarding_title), style = DejaViewType.headline)
            Text(stringResource(R.string.onboarding_body), style = DejaViewType.body, color = DejaViewColors.secondaryText)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                OnboardingMode(UiR.drawable.dv_search_text, R.string.onboarding_text_mode)
                OnboardingMode(UiR.drawable.dv_search_image, R.string.onboarding_image_mode)
                OnboardingMode(UiR.drawable.dv_search_video, R.string.onboarding_video_mode)
                OnboardingMode(UiR.drawable.dv_search_audio, R.string.onboarding_audio_mode)
            }
            Spacer(Modifier.weight(1f))
            PrimaryButton(stringResource(R.string.onboarding_continue), onContinue, Modifier.fillMaxWidth())
            Text(
                stringResource(R.string.onboarding_guest_hint),
                Modifier.fillMaxWidth(),
                color = DejaViewColors.mutedText,
                style = DejaViewType.caption,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun OnboardingMode(
    @DrawableRes icon: Int,
    @StringRes label: Int,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(DejaViewSpacing.small)) {
        Box(Modifier.size(48.dp).background(DejaViewColors.surface, DejaViewShapes.field), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = null, tint = DejaViewColors.accent, modifier = Modifier.size(23.dp))
        }
        Text(stringResource(label), color = DejaViewColors.secondaryText, style = DejaViewType.caption)
    }
}
