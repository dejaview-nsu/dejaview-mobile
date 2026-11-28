package ru.dejaview.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import ru.dejaview.contracts.session.SessionController
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.media.AudioDraftStore
import javax.inject.Inject

@HiltAndroidApp
class DejaViewApplication : Application() {
    @Inject lateinit var sessions: SessionController

    @Inject lateinit var audioDrafts: AudioDraftStore

    @Inject lateinit var search: SearchServices

    override fun onCreate() {
        super.onCreate()
        val startup = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        startup.launch { audioDrafts.prepareCache() }
        startup.launch {
            var previousScope: String? = null
            search.session.state.filter { it != SessionState.Initializing }.collect { state ->
                val scope = (state as? SessionState.Authenticated)?.accountScope
                if (previousScope != scope) previousScope?.let { search.history.clear(it) }
                previousScope = scope
            }
        }
        startup.launch { sessions.restore() }
    }
}
