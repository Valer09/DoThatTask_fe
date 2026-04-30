package homeaq.dothattask.dothattask_fe.dothattask_fe.Model

import androidx.compose.runtime.Composable
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.App
import io.ktor.client.HttpClient
import org.jetbrains.compose.ui.tooling.preview.Preview

fun client(): HttpClient = createHttpClient(onRefreshFailed = { AuthState.onSessionExpired?.invoke() })

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}