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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createUnauthenticatedClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.ToastMessage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.WebLoginAutofillBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.log

@Composable
fun LoginPage(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authApi = remember { AuthApi(createUnauthenticatedClient(), client()) }

    var loading by remember { mutableStateOf(false) }
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val loginButtonFocusRequester = remember { FocusRequester() }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    // Counter incremented after a successful login so the web autofill
    // bridge can fire the hidden form's `submit` event — that's what
    // triggers the browser's "save credentials" prompt.
    var autofillSubmitTrigger by remember { mutableStateOf(0) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    WebLoginAutofillBridge(
        username = username,
        password = password,
        onUsernameChange = { username = it.filter { ch -> !ch.isWhitespace() } },
        onPasswordChange = { password = it },
        submitTrigger = autofillSubmitTrigger,
    )

    fun validateUsername(): Boolean {
        usernameError = when {
            username.isBlank() -> "Username cannot be empty"
            username.length < 3 -> "Username must be at least 3 characters"
            username.length > 50 -> "Username too long"
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Only letters, numbers and underscore allowed"
            else -> null
        }
        return usernameError == null
    }
    fun validatePassword(): Boolean {
        passwordError = when {
            password.isBlank() -> "Password cannot be empty"
            else -> null
        }
        return passwordError == null
    }

    LoadingOverlay(isLoading = loading)

    Column(modifier = Modifier.padding(top = 150.dp).padding(horizontal = 30.dp)) {
        toastMessage?.let {
            ToastMessage(
                message = it,
                isError = toastIsError,
                onDismiss = { toastMessage = null }
            )
        }

        Text(
            "Welcome in DO THAT TASK!",
            style = MaterialTheme.typography.headlineMedium,
            color = TaskUIHelper.getMarinerBlue(),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = username,
            onValueChange = {
                // Strip every whitespace character: usernames are a single
                // token (regex below rejects spaces) and we want
                // `valerio99 ` to behave like `valerio99` even when the user
                // accidentally appended a trailing space (or autofill did).
                username = it.filter { ch -> !ch.isWhitespace() }
                if (usernameError != null) validateUsername()
            },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
                .focusRequester(usernameFocusRequester)
                .focusProperties { next = passwordFocusRequester }
                .semantics { contentType = ContentType.Username },
            supportingText = usernameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            isError = usernameError != null,
            singleLine = true,
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (passwordError != null) validatePassword()
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
                .focusRequester(passwordFocusRequester)
                .onPreviewKeyEvent { event ->
                    if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                        loginButtonFocusRequester.requestFocus()
                        true
                    } else false
                }
                .semantics { contentType = ContentType.Password },
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val isUsernameValid = validateUsername()
                val isPasswordValid = validatePassword()
                if (isUsernameValid && isPasswordValid) {
                    loading = true
                    CoroutineScope(Dispatchers.Default).launch {
                        try {
                            when (val response = authApi.login(username.trim(), password)) {
                                is ApiResult.Success -> {
                                    errorMessage = null
                                    autofillSubmitTrigger += 1
                                    onLoginSuccess()
                                    println("Success")
                                }
                                is ApiResult.Error -> {
                                    toastIsError = true
                                    toastMessage = response.message
                                    errorMessage = response.message
                                    AuthState.clear()
                                    println("Error")
                                }
                                is ApiResult.NotFound -> {
                                    errorMessage = "Login endpoint unavailable"
                                    AuthState.clear()
                                    println("NotFound")
                                }
                                is ApiResult.Unauthorized -> {
                                    errorMessage = "Unauthorized"
                                    AppState.currentScreen = Screen.Login
                                    println("Unauthorized")
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage = "Login failed: ${e.message}"
                            AuthState.clear()
                        } finally {
                            loading = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
                .pointerHoverIcon(PointerIcon.Hand, true)
                .focusRequester(loginButtonFocusRequester)
                .focusable()
                .focusProperties { next = usernameFocusRequester },
        ) {
            Text("Login")
        }

        errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Don't have an account? Register",
            color = TaskUIHelper.getMarinerBlue(),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .pointerHoverIcon(PointerIcon.Hand, true)
                .clickable { AppState.currentScreen = Screen.Register },
        )
    }
}
