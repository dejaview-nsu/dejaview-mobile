package ru.dejaview.audio.data

import android.media.MediaPlayer
import android.media.MediaRecorder
import ru.dejaview.media.AUDIO_MS
import ru.dejaview.media.AudioDraftStore
import java.io.File

private const val SAMPLE_RATE_HZ = 48_000
private const val AAC_SAMPLES_PER_FRAME = 1024

// MPEG4Writer signals the limit after a frame is written. Reserve two frames for finalization.
private const val FINAL_FRAMES_MS = (2 * AAC_SAMPLES_PER_FRAME * 1000 + SAMPLE_RATE_HZ - 1) / SAMPLE_RATE_HZ

internal class AudioDevices(
    private val store: AudioDraftStore,
) {
    private var recorder: MediaRecorder? = null
    private var output: File? = null
    private var player: MediaPlayer? = null

    @Suppress("DEPRECATION")
    fun record(
        finished: () -> Unit,
        failed: () -> Unit,
    ) {
        stopPlayback()
        val file = store.temporary()
        output = file
        val mic = MediaRecorder()
        recorder = mic
        mic.setAudioSource(MediaRecorder.AudioSource.MIC)
        mic.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mic.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mic.setAudioSamplingRate(SAMPLE_RATE_HZ)
        mic.setAudioChannels(1)
        mic.setAudioEncodingBitRate(96_000)
        mic.setOutputFile(file.absolutePath)
        mic.setMaxDuration(AUDIO_MS.toInt() - FINAL_FRAMES_MS)
        mic.setOnInfoListener { source, what, _ ->
            if (source === recorder && what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) finished()
        }
        mic.setOnErrorListener { source, _, _ -> if (source === recorder) failed() }
        mic.prepare()
        mic.start()
    }

    fun finish(): File? {
        val mic = recorder ?: return null
        recorder = null
        val file = output
        output = null
        try {
            mic.stop()
        } catch (_: RuntimeException) {
            file?.delete()
            return null
        } finally {
            mic.release()
        }
        return file
    }

    fun recordingAmplitude(): Int =
        try {
            recorder?.maxAmplitude ?: 0
        } catch (_: IllegalStateException) {
            0
        }

    fun playbackPositionMs(): Int =
        try {
            player?.currentPosition ?: 0
        } catch (_: IllegalStateException) {
            0
        }

    fun play(
        id: String,
        stopped: () -> Unit,
        failed: () -> Unit,
    ) {
        stopPlayback()
        val media = MediaPlayer()
        player = media
        media.setDataSource(store.file(id).absolutePath)
        media.setOnPreparedListener { if (media === player) media.start() }
        media.setOnCompletionListener {
            if (media === player) {
                stopPlayback()
                stopped()
            }
        }
        media.setOnErrorListener { _, _, _ ->
            if (media === player) {
                stopPlayback()
                failed()
            }
            true
        }
        media.prepareAsync()
    }

    fun stopPlayback() {
        player?.release()
        player = null
    }

    fun close() {
        val mic = recorder
        recorder = null
        try {
            mic?.release()
        } finally {
            output?.delete()
            output = null
            stopPlayback()
        }
    }
}
