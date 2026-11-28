package ru.dejaview.audio.presentation

import android.net.Uri
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.dejaview.audio.data.AudioDevices
import ru.dejaview.audio.domain.AudioSearch
import ru.dejaview.contracts.AppError
import ru.dejaview.contracts.AppResult
import ru.dejaview.contracts.media.PreparedMedia
import ru.dejaview.media.AudioDraftStore
import ru.dejaview.model.SearchCompletion
import ru.dejaview.model.SearchMode
import java.io.IOException
import java.time.Instant
import java.util.UUID

internal enum class AudioPhase { Ready, Importing, Recording, Playing, Uploading, Processing }

internal data class AudioState(
    val phase: AudioPhase = AudioPhase.Ready,
    val draft: PreparedMedia? = null,
    val sent: Long = 0,
    val elapsedMs: Long = 0,
    val recordingLevels: List<Float> = emptyList(),
    val waveform: List<Float> = emptyList(),
    val error: AppError? = null,
    val completion: SearchCompletion? = null,
)

internal class AudioViewModel(
    private val store: AudioDraftStore,
    private val search: AudioSearch,
    draftId: String?,
) : ViewModel() {
    private val devices = AudioDevices(store)
    private val mutable = MutableStateFlow(AudioState())
    val state = mutable.asStateFlow()
    private var operation: Job? = null

    @Volatile private var revision = 0L

    init {
        if (draftId != null) load { store.metadata(draftId) }
    }

    fun pick(uri: Uri) = load { store.importDocument(uri) }

    private fun load(waveform: List<Float> = emptyList(), source: suspend () -> AppResult<PreparedMedia>) {
        resetOperation()
        mutable.update { it.copy(phase = AudioPhase.Importing, error = null) }
        operation =
            viewModelScope.launch {
                var imported: PreparedMedia? = null
                try {
                    val result =
                        withContext(Dispatchers.IO) {
                            source().also { if (it is AppResult.Success) imported = it.value }
                        }
                    when (result) {
                        is AppResult.Success -> {
                            mutable.value.draft
                                ?.takeUnless { it.id == result.value.id }
                                ?.let { store.discard(it.id) }
                            mutable.value = AudioState(draft = result.value, waveform = waveform)
                            imported = null
                        }
                        is AppResult.Failure -> fail(result.error)
                    }
                } catch (_: IOException) {
                    fail(AppError.InvalidFile("FILE_CORRUPTED"))
                } catch (_: SecurityException) {
                    fail(AppError.InvalidFile("FILE_ACCESS"))
                } finally {
                    imported?.let { store.discard(it.id) }
                }
            }
    }

    fun record() {
        resetOperation()
        try {
            devices.record(::finishRecording) {
                interrupt()
                fail(AppError.InvalidFile("MIC_BUSY"))
            }
            mutable.update { it.copy(phase = AudioPhase.Recording, error = null, elapsedMs = 0, recordingLevels = emptyList()) }
            monitorRecording()
        } catch (_: IOException) {
            interrupt()
            fail(AppError.InvalidFile("MIC_BUSY"))
        } catch (_: RuntimeException) {
            interrupt()
            fail(AppError.InvalidFile("MIC_BUSY"))
        }
    }

    fun finishRecording() {
        val file = devices.finish()
        if (file == null) {
            fail(AppError.InvalidFile("MIC_BUSY"))
        } else {
            load(mutable.value.recordingLevels) { store.register(file) }
            operation?.invokeOnCompletion { store.discardUnregistered(file) }
        }
    }

    fun play() {
        val draft = mutable.value.draft ?: return
        resetOperation()
        try {
            devices.play(draft.id, { mutable.update { it.copy(phase = AudioPhase.Ready) } }, { fail(AppError.InvalidFile("PLAYBACK")) })
            mutable.update { it.copy(phase = AudioPhase.Playing, error = null, elapsedMs = 0) }
            operation = viewModelScope.launch {
                while (mutable.value.phase == AudioPhase.Playing) {
                    mutable.update { it.copy(elapsedMs = devices.playbackPositionMs().toLong()) }
                    delay(100)
                }
            }
        } catch (_: IOException) {
            interrupt()
            fail(AppError.InvalidFile("PLAYBACK"))
        } catch (_: RuntimeException) {
            interrupt()
            fail(AppError.InvalidFile("PLAYBACK"))
        }
    }

    private fun monitorRecording() {
        val started = SystemClock.elapsedRealtime()
        operation = viewModelScope.launch {
            while (mutable.value.phase == AudioPhase.Recording) {
                val amplitude = (devices.recordingAmplitude() / 32767f).coerceIn(0f, 1f)
                mutable.update {
                    it.copy(
                        elapsedMs = SystemClock.elapsedRealtime() - started,
                        recordingLevels = (it.recordingLevels + amplitude).takeLast(50),
                    )
                }
                delay(200)
            }
        }
    }

    fun submit() {
        val initial = mutable.value
        val draft = initial.draft ?: return
        if (initial.phase != AudioPhase.Ready || initial.completion != null) return
        val attempt = ++revision
        mutable.update { it.copy(phase = AudioPhase.Uploading, sent = 0, error = null) }
        operation =
            viewModelScope.launch {
                val result =
                    withContext(Dispatchers.IO) {
                        search.search(draft) { bytes ->
                            mutable.update {
                                if (revision != attempt) {
                                    it
                                } else {
                                    it.copy(
                                        sent = bytes,
                                        phase = if (bytes >= draft.byteCount) AudioPhase.Processing else AudioPhase.Uploading,
                                    )
                                }
                            }
                        }
                    }
                if (revision != attempt) return@launch
                when (result) {
                    is AppResult.Success -> {
                        store.discard(draft.id)
                        mutable.value =
                            AudioState(
                                completion =
                                    SearchCompletion(
                                        UUID.randomUUID().toString(),
                                        SearchMode.AUDIO,
                                        result.value,
                                        draft.displayName,
                                        Instant.now(),
                                    ),
                            )
                    }
                    is AppResult.Failure -> fail(result.error)
                }
            }
    }

    fun takeCompletion(): SearchCompletion? = mutable.value.completion?.also { mutable.value = AudioState() }

    fun cancelUpload() = resetOperation()

    fun remove() {
        resetOperation()
        mutable.value.draft?.let { store.discard(it.id) }
        mutable.value = AudioState()
    }

    fun interrupt() {
        if (mutable.value.phase in setOf(AudioPhase.Recording, AudioPhase.Playing)) {
            mutable.update { it.copy(phase = AudioPhase.Ready) }
        }
        devices.close()
    }

    private fun resetOperation() {
        revision++
        operation?.cancel()
        devices.close()
        mutable.update { it.copy(phase = AudioPhase.Ready, sent = 0) }
    }

    private fun fail(error: AppError) {
        mutable.update { it.copy(phase = AudioPhase.Ready, error = error) }
    }

    override fun onCleared() {
        remove()
    }
}
