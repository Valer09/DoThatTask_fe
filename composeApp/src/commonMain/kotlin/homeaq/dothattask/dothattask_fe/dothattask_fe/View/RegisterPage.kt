package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.NotificationApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createUnauthenticatedClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RegisterPage(onRegisterSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val authApi = remember { AuthApi(createUnauthenticatedClient(), client()) }
    val focusManager = LocalFocusManager.current

    fun validate(): Boolean {
        val trimmedName = name.trim()
        nameError = if (trimmedName.isBlank()) "Name cannot be empty" else null
        usernameError = when {
            username.isBlank() -> "Username cannot be empty"
            username.length < 3 -> "Username must be at least 3 characters"
            username.length > 50 -> "Username too long"
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Only letters, numbers and underscore allowed"
            else -> null
        }
        passwordError = when {
            password.isBlank() -> "Password cannot be empty"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        confirmError = if (confirmPassword != password) "Passwords do not match" else null
        return listOf(nameError, usernameError, passwordError, confirmError).all { it == null }
    }

    LoadingOverlay(isLoading = loading)

    Column(modifier = Modifier.padding(top = 100.dp).padding(horizontal = 30.dp)) {
        Text(
            "Create your account",
            style = MaterialTheme.typography.headlineMedium,
            color = TaskUIHelper.getMarinerBlue(),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (nameError != null) nameError = if (it.isBlank()) "Name cannot be empty" else null
            },
            label = { Text("Name") },
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
                .semantics { contentType = ContentType.PersonFullName },
            singleLine = true,
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = username,
            onValueChange = {
                // Strip whitespace: usernames are single tokens, regex below
                // rejects spaces. Keeps `valerio99 ` equivalent to `valerio99`.
                username = it.filter { ch -> !ch.isWhitespace() }
                usernameError = null
            },
            label = { Text("Username") },
            isError = usernameError != null,
            supportingText = usernameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
                .semantics { contentType = ContentType.NewUsername },
            singleLine = true,
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
                .semantics { contentType = ContentType.NewPassword },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmError = null },
            label = { Text("Confirm password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = confirmError != null,
            supportingText = confirmError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
                .semantics { contentType = ContentType.NewPassword },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (!validate()) return@Button
                loading = true
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        when (val resp = authApi.register(name.trim(), username.trim(), password)) {
                            is ApiResult.Success -> {
                                errorMessage = null
                                onRegisterSuccess()
                            }
                            is ApiResult.Error -> if (!resp.routeIfNetwork()) errorMessage = resp.message
                            is ApiResult.NotFound -> errorMessage = "Registration endpoint unavailable"
                            is ApiResult.Unauthorized -> {
                                errorMessage = "Unauthorized"
                                AppState.currentScreen = Screen.Login
                            }
                        }

                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Registration failed"
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Hand, true).focusable(),
        ) {
            Text("Create account")
        }

        errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Already have an account? Log in",
            color = TaskUIHelper.getMarinerBlue(),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .pointerHoverIcon(PointerIcon.Hand, true)
                .clickable { AppState.currentScreen = Screen.Login },
        )
    }
}
