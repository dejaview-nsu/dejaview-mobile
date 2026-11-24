package ru.dejaview.audio

import android.content.Context
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import ru.dejaview.audio.domain.AudioSearch
import ru.dejaview.audio.presentation.AudioScreen
import ru.dejaview.audio.presentation.AudioViewModel
import ru.dejaview.contracts.media.MediaStore
import ru.dejaview.contracts.session.SessionReader
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.media.AudioDraftStore
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode
import ru.dejaview.search.api.SearchModalityUi
import ru.dejaview.search.api.SearchModeKey
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface AudioModule {
    @Binds fun mediaStore(store: AudioDraftStore): MediaStore

    @Binds @IntoMap
    @SearchModeKey(SearchMode.AUDIO)
    fun audio(mode: AudioMode): SearchModalityUi

    companion object {
        @Provides @Singleton
        fun drafts(
            @ApplicationContext context: Context,
        ) = AudioDraftStore(context)
    }
}

internal class AudioMode
    @Inject
    constructor(
        private val drafts: AudioDraftStore,
        private val search: AudioSearch,
        private val reader: SessionReader,
    ) : SearchModalityUi {
        override val mode = SearchMode.AUDIO

        @Composable
        override fun Content(
            draftId: String?,
            onCompleted: (SearchCompletion) -> Unit,
            modifier: Modifier,
        ) {
            val session by reader.state.collectAsStateWithLifecycle()
            val account = session as? SessionState.Authenticated ?: return
            val model = viewModel<AudioViewModel>(key = account.accountScope) { AudioViewModel(drafts, search, draftId) }
            val activity = LocalActivity.current
            DisposableEffect(model) { onDispose { if (activity?.isChangingConfigurations != true) model.remove() } }
            val state by model.state.collectAsStateWithLifecycle()
            LifecycleStartEffect(model) { onStopOrDispose { model.interrupt() } }
            LaunchedEffect(state.completion) { model.takeCompletion()?.let(onCompleted) }
            AudioScreen(state, model, modifier)
        }
    }
