package ru.dejaview.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.dejaview.contracts.session.SessionController
import ru.dejaview.media.AudioDraftStore
import javax.inject.Inject

@HiltAndroidApp
class DejaViewApplication : Application() {
    @Inject lateinit var sessions: SessionController

    @Inject lateinit var audioDrafts: AudioDraftStore

    override fun onCreate() {
        super.onCreate()
        val startup = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        startup.launch { audioDrafts.prepareCache() }
        startup.launch { sessions.restore() }
    }
}
