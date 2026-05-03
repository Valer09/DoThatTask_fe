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
    // iOS handles autofill natively via Modifier.semantics { contentType = ... }
    // (Compose forwards it as UITextContentType on UITextField).
}
