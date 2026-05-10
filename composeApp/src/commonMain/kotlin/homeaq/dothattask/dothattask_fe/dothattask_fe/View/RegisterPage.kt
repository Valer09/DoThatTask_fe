package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocalStrings
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createUnauthenticatedClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

private val EmailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")

@Composable
@Preview
fun RegisterPage(onRegisterSuccess: () -> Unit) {
    val s = LocalStrings.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val authApi = remember { AuthApi(createUnauthenticatedClient(), client()) }
    val focusManager = LocalFocusManager.current

    fun validate(): Boolean {
        val trimmedName = name.trim()
        nameError = if (trimmedName.isBlank()) s.registerNameEmpty else null
        emailError = when {
            email.isBlank() -> s.registerEmailEmpty
            email.length > 320 -> s.loginEmailTooLong
            !EmailRegex.matches(email.trim()) -> s.registerEmailInvalid
            else -> null
        }
        // Username is optional; only validate the format when supplied so
        // that the BE-side derivation from the email local-part can run.
        usernameError = when {
            username.isBlank() -> null
            username.length < 3 -> "Username must be at least 3 characters"
            username.length > 50 -> "Username too long"
            !username.matches(Regex("^[a-zA-Z0-9_]+\$")) -> "Only letters, numbers and underscore allowed"
            else -> null
        }
        passwordError = when {
            password.isBlank() -> s.registerPasswordEmpty
            password.length < 6 -> s.registerPasswordTooShort
            else -> null
        }
        confirmError = if (confirmPassword != password) s.changePasswordMismatch else null
        return listOf(nameError, emailError, usernameError, passwordError, confirmError).all { it == null }
    }

    LoadingOverlay(isLoading = loading)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .safeDrawingPadding()
            .imePadding()
            .padding(top = 16.dp)
            .padding(horizontal = 20.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = TaskUIHelper.appCardShape(),
            colors = TaskUIHelper.appCardColors(),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    s.registerTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = if (it.isBlank()) s.registerNameEmpty else null
                    },
                    label = { Text(s.registerName) },
                    colors = TaskUIHelper.appTextFieldColors(),
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                        .semantics { contentType = ContentType.PersonFullName },
                    singleLine = true,
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it.filter { ch -> !ch.isWhitespace() }
                        emailError = null
                    },
                    label = { Text(s.registerEmail) },
                    colors = TaskUIHelper.appTextFieldColors(),
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                        .semantics { contentType = ContentType.EmailAddress },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    singleLine = true,
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it.filter { ch -> !ch.isWhitespace() }
                        usernameError = null
                    },
                    label = { Text(s.registerUsernameOptional) },
                    placeholder = { Text(s.registerUsernameHint) },
                    colors = TaskUIHelper.appTextFieldColors(),
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
                    label = { Text(s.registerPassword) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TaskUIHelper.appTextFieldColors(),
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
                    label = { Text(s.changePasswordConfirm) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TaskUIHelper.appTextFieldColors(),
                    isError = confirmError != null,
                    supportingText = confirmError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                        .semantics { contentType = ContentType.NewPassword },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                )

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (!validate()) return@Button
                        loading = true
                        CoroutineScope(Dispatchers.Default).launch {
                            try {
                                when (val resp = authApi.register(
                                    name = name.trim(),
                                    email = email.trim(),
                                    password = password,
                                    username = username.trim().ifBlank { null },
                                )) {
                                    is ApiResult.Success -> {
                                        errorMessage = null
                                        infoMessage = s.registerSuccess
                                        onRegisterSuccess()
                                    }
                                    is ApiResult.Error -> if (!resp.routeIfNetwork()) errorMessage = resp.message
                                    is ApiResult.NotFound -> errorMessage = s.loginEndpointUnavailable
                                    is ApiResult.Unauthorized -> {
                                        errorMessage = s.unauthorized
                                        AppState.currentScreen = Screen.Login
                                    }
                                    is ApiResult.Forbidden -> {
                                        errorMessage = s.forbidden
                                    }
                                }

                            } catch (e: Exception) {
                                errorMessage = e.message ?: s.loginFailed
                            } finally {
                                loading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskUIHelper.getComplementary(),
                        contentColor = Color.Black,
                    ),
                    modifier = Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Hand, true).focusable(),
                ) {
                    Text(s.registerButton)
                }

                infoMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }

                errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    s.registerHaveAccount,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .pointerHoverIcon(PointerIcon.Hand, true)
                        .clickable { AppState.currentScreen = Screen.Login },
                )
            }
        }
    }
}
