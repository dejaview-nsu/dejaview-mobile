package ru.dejaview.search.presentation

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import ru.dejaview.core.ui.R

internal fun String?.httpsPoster(): String? =
    this?.takeIf { url -> Uri.parse(url).let { it.scheme == "https" && !it.host.isNullOrBlank() } }

@Composable
internal fun ResultPoster(url: String?, modifier: Modifier = Modifier) {
    val fallback = painterResource(R.drawable.dv_poster_placeholder)
    AsyncImage(
        model = url.httpsPoster(),
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        placeholder = fallback,
        error = fallback,
        fallback = fallback,
        contentScale = ContentScale.Crop,
    )
}
