package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.runtime.Composable

@Composable
actual fun WebLoginAutofillBridge(
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    submitTrigger: Int,
) {
    // Desktop has no equivalent OS-level password manager hook.
}
