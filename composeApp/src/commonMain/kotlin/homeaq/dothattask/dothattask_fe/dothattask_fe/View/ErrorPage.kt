package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen

/**
 * Full-screen fallback shown after a non-auth failure (network unreachable,
 * server 5xx, decoding error, …). Renders the message stashed in
 * `AppState.errorMessage` and offers a single "Home" button that puts the
 * user back on the main screen.
 *
 * Auth-related failures (Unauthorized, expired refresh token) intentionally
 * don't route here — they keep flowing through `AuthState.onSessionExpired`
 * which redirects to the login page.
 */
@Composable
fun ErrorPage(
    onHome: () -> Unit = {
        // Default destination depends on session state: drop to Login if the
        // user isn't authenticated, otherwise pick the right "main" screen
        // based on whether they belong to any group.
        AppState.errorMessage = null
        AppState.currentScreen = when {
            AuthState.accessToken == null -> Screen.Login
            AuthState.groups.isNotEmpty() -> Screen.Home
            else -> Screen.NoGroup
        }
    },
) {
    val message = AppState.errorMessage

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
        ) {
            Text(
                "⚠",
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.error,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TaskUIHelper.getMarinerBlue(),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                message ?: "We couldn't reach the server. Check your connection and try again.",
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onHome,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskUIHelper.getMarinerBlue(),
                    contentColor = Color.White,
                ),
            ) {
                Text("Home")
            }
        }
    }
}
