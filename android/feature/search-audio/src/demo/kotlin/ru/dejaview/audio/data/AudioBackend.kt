package ru.dejaview.audio.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import ru.dejaview.audio.domain.AudioSearch
import ru.dejaview.contracts.AppResult
import ru.dejaview.model.MovieSummary

@Module
@InstallIn(SingletonComponent::class)
internal object AudioBackend {
    @Provides fun search(): AudioSearch =
        AudioSearch { media, progress ->
            delay(150)
            progress(media.byteCount)
            delay(200)
            val results = if ((media.durationMs ?: 0) < 1000) emptyList() else listOf(MovieSummary(1, "Сигнал с далёкой орбиты"))
            AppResult.Success(results)
        }
}
