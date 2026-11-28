package ru.dejaview.media

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import ru.dejaview.contracts.AppResult
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ContentAudioTest {
    @Test fun startupRemovesAbandonedFilesAndKeepsCurrentRecordings() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = java.io.File(context.cacheDir, "audio-drafts")
        directory.mkdirs()
        val abandoned = java.io.File.createTempFile("old-", ".tmp", directory)
        val store = AudioDraftStore(context)
        store.prepareCache()
        assertFalse(abandoned.exists())
        val recording = store.temporary()
        try {
            store.prepareCache()
            assertTrue(recording.isFile)
        } finally {
            store.discardUnregistered(recording)
        }
    }

    @Test fun readsRealContentUriValidatesContainerAndDeletesCopy() =
        runBlocking {
            assumeTrue(Build.VERSION.SDK_INT >= 29)
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val store = AudioDraftStore(context)
            val resolver = context.contentResolver
            val values =
                ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, "synthetic.wav")
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
            val uri = requireNotNull(resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values))
            try {
                // One second of synthetic mono PCM inside a WAV container.
                val header =
                    ByteBuffer
                        .allocate(44)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .apply {
                            put("RIFF".toByteArray())
                            putInt(16036)
                            put("WAVEfmt ".toByteArray())
                            putInt(16)
                            putShort(1)
                            putShort(1)
                            putInt(8000)
                            putInt(16000)
                            putShort(2)
                            putShort(16)
                            put("data".toByteArray())
                            putInt(16000)
                        }.array()
                requireNotNull(resolver.openOutputStream(uri)).use {
                    it.write(header)
                    it.write(ByteArray(16000))
                }
                val result = store.importDocument(uri) as AppResult.Success
                assertEquals(1000L, result.value.durationMs)
                assertEquals("audio/wav", result.value.mimeType)
                val copy = store.file(result.value.id)
                assertEquals(16044L, copy.length())
                store.release(result.value.id)
                assertFalse(copy.exists())
                requireNotNull(resolver.openOutputStream(uri, "wt")).use { it.write("invalid audio".toByteArray()) }
                assertTrue(store.importDocument(uri) is AppResult.Failure)
            } finally {
                resolver.delete(uri, null, null)
            }
            Unit
        }
}
