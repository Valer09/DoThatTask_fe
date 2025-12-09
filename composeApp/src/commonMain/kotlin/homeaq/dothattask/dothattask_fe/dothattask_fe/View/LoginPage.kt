package homeaq.dothattask.dothattask_fe.dothattask_fe.View

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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginPage(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val taskApi = remember { TaskApi(createHttpClient()) }

    var loading by remember { mutableStateOf(false) }
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val loginButtonFocusRequester = remember { FocusRequester() }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

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
            //password.length < 8 -> "Password must be at least 8 characters"
            else -> null
        }
        return passwordError == null
    }

    LoadingOverlay(isLoading = loading)

    Column(modifier = Modifier.padding(top = 150.dp).padding(horizontal = 30.dp)) {


        Text("Welcome in DO THAT TASK!", style = MaterialTheme.typography.headlineMedium, color = TaskUIHelper.getMarinerBlue(), fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = username,
            onValueChange =
                {
                    username = it
                    if (usernameError != null) validateUsername()
                },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
                .focusRequester(usernameFocusRequester).focusRequester(usernameFocusRequester)
                .focusProperties {
                    next = passwordFocusRequester
                },
            supportingText = usernameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            isError = usernameError != null,
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange =
                {
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
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val isUsernameValid = validateUsername()
                val isPasswordValid = validatePassword()
                if(isUsernameValid && isPasswordValid)
                {
                    loading = true
                    try {
                        AuthState.setCredentials(username, password)
                        CoroutineScope(Dispatchers.Default).launch {
                            try {
                                val response = taskApi.getAllTasksDb()

                                if(response is ApiResult.Success)
                                {
                                    errorMessage = null
                                    AppState.currentScreen = Screen.Home
                                    onLoginSuccess()
                                }
                                else if(response is ApiResult.Error)
                                {
                                    println(response.message)
                                    errorMessage = "Login failed"
                                    AuthState.clear()
                                }
                                else if(response is ApiResult.NotFound)
                                {
                                    println(response.message)
                                    errorMessage = "Login failed"
                                    AuthState.clear()
                                }
                            }
                            catch (e: Exception)
                            {
                                println(e.message)
                                errorMessage = "Login failed: ${e.message}"
                                AuthState.clear()
                            }
                            finally
                            {
                                loading = false
                            }
                        }
                    }
                    catch (e: Exception)
                    {
                        errorMessage = e.message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Hand, true).focusRequester(loginButtonFocusRequester)
                .focusable()
                .focusProperties {
                    next = usernameFocusRequester
                }
        ) {
            Text("Login")
        }

        errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
