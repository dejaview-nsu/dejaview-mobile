package ru.dejaview.app

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import ru.dejaview.auth.domain.LoginResult
import ru.dejaview.contracts.session.SessionState

class RecordingLimitSmoke {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun automaticStopLeavesAPlayableDraftWithinTheServerLimit() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("recordSmoke") == "true")
        val activity = compose.activity
        val sessions = activity.sessions
        compose.waitUntil { sessions.reader.state.value != SessionState.Initializing }
        InstrumentationRegistry
            .getInstrumentation()
            .uiAutomation
            .grantRuntimePermission(activity.packageName, Manifest.permission.RECORD_AUDIO)
        try {
            assertEquals(LoginResult.Success, runBlocking { sessions.login.login("viewer", "x", null) })
            compose.onNodeWithText("Звук").performClick()
            compose.onNodeWithText("Записать звук").performClick()
            compose.onNodeWithText("Завершить запись").assertIsDisplayed()
            compose.waitUntil(16_000) { compose.onAllNodes(hasText("Прослушать")).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Прослушать").performClick()
            compose.onNodeWithText("Остановить воспроизведение").performClick()
            compose.onNodeWithText("Удалить фрагмент").performClick()
            compose.onNodeWithText("Прослушать").assertDoesNotExist()
        } finally {
            runBlocking { sessions.controller.logout() }
        }
    }
}
