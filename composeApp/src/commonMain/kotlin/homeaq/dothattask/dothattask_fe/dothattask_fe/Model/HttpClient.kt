package homeaq.dothattask.dothattask_fe.dothattask_fe.Model

import androidx.compose.runtime.Composable
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.App
import io.ktor.client.HttpClient
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Process-wide singleton authenticated client. Sharing one instance lets the
 * Ktor Auth plugin coalesce concurrent 401s into a single refresh round-trip
 * (it serialises the [refreshTokens] callback). This is critical because the
 * backend rotates refresh tokens single-use: parallel refreshes would race,
 * one would win, and the others would receive 401s on a now-revoked token —
 * causing spurious logouts on every app cold start, when several screens
 * (and on Android the FCM/onResume callbacks) all fire authenticated calls
 * simultaneously.
 */
private val sharedClient: HttpClient by lazy {
    createHttpClient(onRefreshFailed = { AuthState.onSessionExpired?.invoke() })
}

fun client(): HttpClient = sharedClient

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}