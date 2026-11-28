package ru.dejaview.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AudioLimitsTest {
    @Test fun acceptsInclusiveLimitsAndRejectsUnknownDuration() {
        assertNull(validateAudio(20L * 1024 * 1024, 10_000))
        assertEquals("FILE_TOO_LARGE", validateAudio(AUDIO_BYTES + 1, 10_000))
        assertEquals("AUDIO_TOO_LONG", validateAudio(1, 10_001))
        assertEquals("FILE_CORRUPTED", validateAudio(0, 100))
        assertEquals("FILE_CORRUPTED", validateAudio(1, 0))
        assertEquals("FILE_CORRUPTED", validateAudio(1, null))
    }
}
