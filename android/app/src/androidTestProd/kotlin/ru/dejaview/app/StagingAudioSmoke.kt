package ru.dejaview.app

import android.content.ContentValues
import android.net.LocalServerSocket
import android.provider.MediaStore
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.app.ActivityOptionsCompat
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeNotNull
import org.junit.Rule
import org.junit.Test
import ru.dejaview.auth.domain.LoginResult
import ru.dejaview.contracts.AppResult
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.core.ui.theme.DejaViewTheme
import ru.dejaview.feature.auth.BuildConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

// Explicit live run on a dedicated emulator. Credentials arrive over an adb-forwarded
// socket and never enter an APK, instrumentation argument, saved state or test report.
class StagingAudioSmoke {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun loginRestoreSearchAndLogout() {
        val socketName = InstrumentationRegistry.getArguments().getString("smokeSocket")
        assumeNotNull(socketName)
        val activity = compose.activity
        val sessions = activity.sessions
        compose.waitUntil { sessions.reader.state.value != SessionState.Initializing }
        LocalServerSocket(requireNotNull(socketName)).use { server ->
            server.accept().use { connection ->
                connection.soTimeout = 10_000
                connection.outputStream.write((BuildConfig.API_ORIGIN + "\n").toByteArray())
                val input = JSONObject(connection.inputStream.bufferedReader().readLine())
                val login = runBlocking { sessions.login.login(input.getString("login"), input.getString("password"), null) }
                if (login is LoginResult.Rejected) error("Live login rejected: " + login.error.code + ", captcha=" + login.requiresCaptcha)
            }
        }
        try {
            val scope = (sessions.reader.state.value as SessionState.Authenticated).accountScope
            assertTrue(runBlocking { sessions.controller.restore() } is AppResult.Success)
            assertEquals(scope, (sessions.reader.state.value as SessionState.Authenticated).accountScope)
            val resolver = activity.contentResolver
            val uri =
                requireNotNull(
                    resolver.insert(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        ContentValues().apply {
                            put(MediaStore.Audio.Media.DISPLAY_NAME, "smoke-audio.wav")
                            put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
                        },
                    ),
                )
            try {
                requireNotNull(resolver.openOutputStream(uri)).use { it.write(sample()) }
                val owner =
                    object : ActivityResultRegistryOwner {
                        override val activityResultRegistry =
                            object : ActivityResultRegistry() {
                                override fun <I, O> onLaunch(
                                    requestCode: Int,
                                    contract: ActivityResultContract<I, O>,
                                    input: I,
                                    options: ActivityOptionsCompat?,
                                ) {
                                    dispatchResult(requestCode, uri)
                                }
                            }
                    }
                compose.runOnUiThread {
                    activity.setContent {
                        CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) {
                            DejaViewTheme { AppNavHost(activity.searchModes, activity.searchServices, sessions.controller) }
                        }
                    }
                }
                compose.onNodeWithText("Звук").performClick()
                compose.onNodeWithText("Выбрать файл").performClick()
                compose.waitUntil(5000) { exists("Прослушать") }
                compose.onNodeWithText("Найти фильм").performClick()
                compose.waitUntil(100_000) {
                    exists("Ничего не нашли") || exists("Найден фильм") || exists("Сервис поиска временно недоступен.") ||
                        exists("Не удалось завершить поиск. Попробуйте ещё раз.") || exists("Файл недоступен или повреждён.")
                }
                compose.onNodeWithText(if (exists("Найден фильм")) "Найден фильм" else "Ничего не нашли").assertIsDisplayed()
            } finally {
                resolver.delete(uri, null, null)
            }
        } finally {
            assertTrue("Server logout was not confirmed", runBlocking { sessions.controller.logout() } is AppResult.Success)
            assertEquals(SessionState.Guest, sessions.reader.state.value)
        }
    }

    private fun exists(text: String) = compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()

    private fun sample(): ByteArray {
        val count = 32_000
        val pcm = ByteBuffer.allocate(44 + 2 * count).order(ByteOrder.LITTLE_ENDIAN)
        pcm
            .put("RIFF".toByteArray())
            .putInt(36 + 2 * count)
            .put("WAVEfmt ".toByteArray())
            .putInt(16)
        pcm
            .putShort(1)
            .putShort(1)
            .putInt(16000)
            .putInt(32000)
            .putShort(2)
            .putShort(16)
        pcm.put("data".toByteArray()).putInt(2 * count)
        repeat(count) { pcm.putShort((sin(it * 2 * Math.PI * 440 / 16000) * 8000).toInt().toShort()) }
        return pcm.array()
    }
}
