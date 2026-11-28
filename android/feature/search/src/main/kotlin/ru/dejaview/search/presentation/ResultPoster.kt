package ru.dejaview.search.presentation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ru.dejaview.core.ui.R
import ru.dejaview.core.ui.theme.DejaViewColors

internal fun String?.httpsPoster(): String? =
    this?.takeIf { url -> Uri.parse(url).let { it.scheme == "https" && !it.host.isNullOrBlank() } }

@Composable
internal fun ResultPoster(
    url: String?,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            painterResource(R.drawable.dv_poster_placeholder),
            contentDescription = null,
            modifier = Modifier.size(25.dp),
            tint = DejaViewColors.mutedText,
        )
        url.httpsPoster()?.let { model ->
            AsyncImage(model, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}
