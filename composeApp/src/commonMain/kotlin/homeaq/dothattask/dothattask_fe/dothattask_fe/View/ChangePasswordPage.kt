package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createUnauthenticatedClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun ChangePasswordPage(onBack: () -> Unit, onPasswordChanged: () -> Unit) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var oldError by remember { mutableStateOf<String?>(null) }
    var newError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val authApi = remember { AuthApi(createUnauthenticatedClient(), client()) }

    fun validate(): Boolean {
        oldError = if (oldPassword.isBlank()) "Current password cannot be empty" else null
        newError = when {
            newPassword.isBlank() -> "New password cannot be empty"
            newPassword.length < 6 -> "New password must be at least 6 characters"
            newPassword == oldPassword -> "New password must differ from the current one"
            else -> null
        }
        confirmError = if (confirmPassword != newPassword) "Passwords do not match" else null
        return listOf(oldError, newError, confirmError).all { it == null }
    }

    LoadingOverlay(isLoading = loading)

    Column(modifier = Modifier.padding(top = 40.dp).padding(horizontal = 30.dp)) {
        Text(
            "Change password",
            style = MaterialTheme.typography.headlineMedium,
            color = TaskUIHelper.getPrimary(),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = oldPassword,
            onValueChange = { oldPassword = it; oldError = null; message = null },
            label = { Text("Current password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = oldError != null,
            supportingText = oldError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
                .semantics { contentType = ContentType.Password },
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it; newError = null; message = null },
            singleLine = true,
            label = { Text("New password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = newError != null,
            supportingText = newError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
                .semantics { contentType = ContentType.NewPassword },
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmError = null; message = null },
            singleLine = true,
            label = { Text("Confirm new password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = confirmError != null,
            supportingText = confirmError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
                .semantics { contentType = ContentType.NewPassword },
        )

        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true).focusable(),
            ) {
                Text("Cancel")
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = {
                    if (!validate()) return@Button
                    loading = true
                    CoroutineScope(Dispatchers.Default).launch {
                        try {
                            when (val resp = authApi.changePassword(oldPassword, newPassword)) {
                                is ApiResult.Success -> {
                                    messageIsError = false
                                    message = "Password changed. Other sessions have been signed out."
                                    oldPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                    onPasswordChanged()
                                }
                                is ApiResult.Error -> if (!resp.routeIfNetwork()) {
                                    messageIsError = true
                                    message = resp.message
                                }
                                is ApiResult.NotFound -> {
                                    messageIsError = true
                                    message = "Change-password endpoint unavailable"
                                }
                                is ApiResult.Unauthorized -> {
                                    messageIsError = true
                                    message = "Unauthorized"
                                    AppState.currentScreen = Screen.Login
                                }
                            }
                        } catch (e: Exception) {
                            messageIsError = true
                            message = e.message ?: "Change password failed"
                        } finally {
                            loading = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TaskUIHelper.getPrimary(), contentColor = Color.White),
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true).focusable(),
            ) {
                Text("Change password")
            }
        }

        message?.let {
            Spacer(Modifier.height(16.dp))
            Text(
                it,
                color = if (messageIsError) MaterialTheme.colorScheme.error else TaskUIHelper.getPrimary(),
            )
        }
    }
}
