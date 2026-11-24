package ru.dejaview.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.time.Instant
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystoreSessionVault(
    context: Context,
) : SessionVault {
    private val file = AtomicFile(File(context.noBackupFilesDir, "dv-session-v1"))
    private val alias = "dv-session-v1"

    override fun read(): StoredSession? =
        try {
            val encrypted =
                file.openRead().use { input ->
                    val size = input.channel.size()
                    if (size !in 29L..65536L) throw IOException("Invalid session envelope")
                    ByteArray(size.toInt()).also { DataInputStream(input).readFully(it) }
                }
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, encrypted, 0, 12))
            DataInputStream(ByteArrayInputStream(cipher.doFinal(encrypted, 12, encrypted.size - 12))).use {
                StoredSession(it.readUTF(), it.readUTF(), it.readUTF(), Instant.ofEpochMilli(it.readLong()))
            }
        } catch (_: java.io.FileNotFoundException) {
            null
        } catch (_: GeneralSecurityException) {
            clear()
            null
        } catch (_: IOException) {
            clear()
            null
        }

    override fun write(session: StoredSession) {
        val plain = ByteArrayOutputStream()
        DataOutputStream(plain).use {
            it.writeUTF(session.cookie)
            it.writeUTF(session.origin)
            it.writeUTF(session.accountScope)
            it.writeLong(session.expiresAt.toEpochMilli())
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.iv + cipher.doFinal(plain.toByteArray())
        val stream = file.startWrite()
        try {
            stream.write(encrypted)
            file.finishWrite(stream)
        } catch (error: IOException) {
            file.failWrite(stream)
            throw error
        }
    }

    override fun clear() {
        file.delete()
        try {
            keyStore().deleteEntry(alias)
        } catch (error: GeneralSecurityException) {
            if (file.baseFile.exists()) throw error
        } catch (error: IOException) {
            if (file.baseFile.exists()) throw error
        }
    }

    private fun keyStore() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun key(): SecretKey {
        val existing = keyStore().getKey(alias, null) as? SecretKey
        if (existing != null) return existing
        val spec =
            KeyGenParameterSpec
                .Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply { init(spec) }.generateKey()
    }
}
