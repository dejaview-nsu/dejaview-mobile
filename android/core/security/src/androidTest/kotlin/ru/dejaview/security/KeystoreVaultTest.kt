package ru.dejaview.security

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.time.Instant
import java.util.UUID

class KeystoreVaultTest {
    @Test fun encryptedEnvelopeSurvivesReopenAndCorruptionClearsIt() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val vault = KeystoreSessionVault(context)
        val marker = UUID.randomUUID().toString()
        try {
            val stored = StoredSession(marker, "https://api.example.test", "test-scope", Instant.now())
            vault.write(stored)
            val file = File(context.noBackupFilesDir, "dv-session-v1")
            assertFalse(file.readBytes().decodeToString().contains(marker))
            val reopened = KeystoreSessionVault(context)
            assertEquals(marker, reopened.read()?.cookie)
            val bytes = file.readBytes()
            bytes[bytes.lastIndex] = bytes.last().inc()
            file.writeBytes(bytes)
            assertNull(reopened.read())
            assertFalse(file.exists())
            reopened.write(stored)
            assertEquals("test-scope", reopened.read()?.accountScope)
        } finally {
            vault.clear()
        }
    }
}
