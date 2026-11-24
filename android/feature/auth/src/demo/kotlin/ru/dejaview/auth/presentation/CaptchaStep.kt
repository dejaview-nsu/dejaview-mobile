package ru.dejaview.auth.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.dejaview.feature.auth.R
import java.util.UUID

internal const val CHALLENGE_AVAILABLE = true

internal object DemoChallenge {
    private var issued: String? = null
    private var issuedAt = 0L

    @Synchronized fun issue(): String =
        UUID.randomUUID().toString().also {
            issued = it
            issuedAt = System.nanoTime()
        }

    @Synchronized fun consume(token: String?): Boolean {
        val valid = token != null && token == issued && System.nanoTime() - issuedAt < 60_000_000_000
        issued = null
        return valid
    }
}

@Composable
internal fun CaptchaStep(
    onCancel: () -> Unit,
    onToken: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.auth_demo_challenge)) },
        confirmButton = { TextButton(onClick = { onToken(DemoChallenge.issue()) }) { Text(stringResource(R.string.auth_confirm)) } },
        dismissButton = { TextButton(onClick = onCancel) { Text(stringResource(R.string.auth_cancel)) } },
    )
}
