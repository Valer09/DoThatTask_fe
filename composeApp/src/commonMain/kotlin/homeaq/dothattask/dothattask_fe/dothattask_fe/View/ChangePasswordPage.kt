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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocalStrings
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.BackButton
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun ChangePasswordPage(onBack: () -> Unit, onPasswordChanged: () -> Unit) {
    val s = LocalStrings.current
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
        oldError = if (oldPassword.isBlank()) s.changePasswordOldEmpty else null
        newError = when {
            newPassword.isBlank() -> s.changePasswordNewEmpty
            newPassword.length < 6 -> s.changePasswordTooShort
            newPassword == oldPassword -> s.changePasswordNewEmpty
            else -> null
        }
        confirmError = if (confirmPassword != newPassword) s.changePasswordMismatch else null
        return listOf(oldError, newError, confirmError).all { it == null }
    }

    LoadingOverlay(isLoading = loading)

    Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp).padding(horizontal = 20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = TaskUIHelper.appCardShape(),
            colors = TaskUIHelper.appCardColors(),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    BackButton(onClick = onBack)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        s.changePasswordTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it; oldError = null; message = null },
                    label = { Text(s.changePasswordOld) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TaskUIHelper.appTextFieldColors(),
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
                    label = { Text(s.changePasswordNew) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TaskUIHelper.appTextFieldColors(),
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
                    label = { Text(s.changePasswordConfirm) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TaskUIHelper.appTextFieldColors(),
                    isError = confirmError != null,
                    supportingText = confirmError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                        .semantics { contentType = ContentType.NewPassword },
                )

                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            if (!validate()) return@Button
                            loading = true
                            CoroutineScope(Dispatchers.Default).launch {
                                try {
                                    when (val resp = authApi.changePassword(oldPassword, newPassword)) {
                                        is ApiResult.Success -> {
                                            messageIsError = false
                                            message = s.changePasswordSuccess
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
                                            message = s.loginEndpointUnavailable
                                        }
                                        is ApiResult.Unauthorized -> {
                                            messageIsError = true
                                            message = s.unauthorized
                                            AppState.currentScreen = Screen.Login
                                        }
                                        is ApiResult.Forbidden -> {
                                            messageIsError = true
                                            message = s.changePasswordWrongOld
                                            AppState.currentScreen = Screen.Login
                                        }
                                    }
                                } catch (e: Exception) {
                                    messageIsError = true
                                    message = e.message ?: s.loginFailed
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TaskUIHelper.getComplementary(),
                            contentColor = Color.Black,
                        ),
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true).focusable(),
                    ) {
                        Text(s.changePasswordSubmit)
                    }
                }

                message?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        it,
                        color = if (messageIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
