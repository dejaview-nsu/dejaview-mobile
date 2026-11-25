package ru.dejaview.app

import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.dejaview.contracts.history.HistoryRepository
import ru.dejaview.contracts.session.SessionReader
import java.util.Optional
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
internal interface OptionalSearchServices {
    @BindsOptionalOf
    fun sessionReader(): SessionReader

    @BindsOptionalOf
    fun history(): HistoryRepository
}

class SearchServices
    @Inject
    internal constructor(
        sessionReader: Optional<SessionReader>,
        historyRepository: Optional<HistoryRepository>,
        internal val defaults: SearchDefaults,
    ) {
        val session: SessionReader = sessionReader.orElse(defaults)
        val history: HistoryRepository = historyRepository.orElse(defaults)
        val demoLogin: (() -> Unit)? = if (sessionReader.isPresent) null else defaults.demoLogin
    }
