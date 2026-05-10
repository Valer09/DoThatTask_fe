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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocalStrings
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createUnauthenticatedClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.ToastMessage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.WebLoginAutofillBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

private val EmailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")

@Composable
@Preview
fun LoginPage(onLoginSuccess: () -> Unit) {
    val s = LocalStrings.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authApi = remember { AuthApi(createUnauthenticatedClient(), client()) }
    var loading by remember { mutableStateOf(false) }
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val loginButtonFocusRequester = remember { FocusRequester() }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var autofillSubmitTrigger by remember { mutableStateOf(0) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }

    WebLoginAutofillBridge(
        username = email,
        password = password,
        onUsernameChange = { email = it.filter { ch -> !ch.isWhitespace() } },
        onPasswordChange = { password = it },
        submitTrigger = autofillSubmitTrigger,
    )

    fun validateEmail(): Boolean {
        emailError = when {
            email.isBlank() -> s.loginEmailEmpty
            email.length > 320 -> s.loginEmailTooLong
            !EmailRegex.matches(email.trim()) -> s.loginEmailInvalid
            else -> null
        }
        return emailError == null
    }

    fun validatePassword(): Boolean {
        passwordError = when {
            password.isBlank() -> s.loginPasswordEmpty
            else -> null
        }
        return passwordError == null
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
        toastMessage?.let {
            ToastMessage(
                message = it,
                isError = toastIsError,
                onDismiss = { toastMessage = null }
            )
        }

        // Hero tagline — same idea as the onboarding step #1, kept here as
        // a permanent reminder of the value prop for returning users.
        // "Start doing." is colored with the complementary orange to draw
        // the eye and contrast the cool purple background.
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = s.loginTaglineLine1,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp,
            )
            Text(
                text = s.loginTaglineLine2,
                color = TaskUIHelper.getComplementary(),
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp,
            )
        }

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = TaskUIHelper.appCardShape(),
            colors = TaskUIHelper.appCardColors(),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    s.loginWelcome,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it.filter { ch -> !ch.isWhitespace() }
                        if (emailError != null) validateEmail()
                    },
                    label = { Text(s.loginEmail) },
                    colors = TaskUIHelper.appTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                        .focusRequester(emailFocusRequester)
                        .focusProperties { next = passwordFocusRequester }
                        .semantics { contentType = ContentType.EmailAddress },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    isError = emailError != null,
                    singleLine = true,
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError != null) validatePassword()
                    },
                    label = { Text(s.loginPassword) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TaskUIHelper.appTextFieldColors(),
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

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        loading = true
                        //val isEmailValid = validateEmail()
                        val isPasswordValid = validatePassword()
                        if (/*isEmailValid && */isPasswordValid) {

                            CoroutineScope(Dispatchers.Default).launch {
                                try {
                                    when (val response = authApi.login(email.trim(), password)) {
                                        is ApiResult.Success -> {
                                            errorMessage = null
                                            autofillSubmitTrigger += 1
                                            onLoginSuccess()
                                        }
                                        is ApiResult.Error -> {
                                            toastIsError = true
                                            toastMessage = if(response.isNetwork) s.loginServerError else response.message
                                            errorMessage = if(response.isNetwork) s.loginServerError else response.message
                                            AuthState.clear()
                                        }
                                        is ApiResult.NotFound -> {
                                            errorMessage = s.loginEndpointUnavailable
                                            AuthState.clear()
                                        }
                                        is ApiResult.Unauthorized -> {
                                            errorMessage = s.loginUnauthorized
                                            AppState.currentScreen = Screen.Login
                                        }

                                        is ApiResult.Forbidden -> {
                                            errorMessage = response.message
                                            AppState.currentScreen = Screen.Login
                                        }

                                    }
                                } catch (e: Exception) {
                                    errorMessage = "${s.loginFailed}: ${e.message}"
                                    AuthState.clear()
                                }
                                finally {loading = false}
                            }
                        } else {
                            loading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskUIHelper.getComplementary(),
                        contentColor = Color.Black,
                    ),
                    modifier = Modifier.fillMaxWidth()
                        .pointerHoverIcon(PointerIcon.Hand, true)
                        .focusRequester(loginButtonFocusRequester)
                        .focusable()
                        .focusProperties { next = emailFocusRequester },
                ) {
                    Text(s.loginButton)
                }

                errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    s.loginNoAccount,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .pointerHoverIcon(PointerIcon.Hand, true)
                        .clickable { AppState.currentScreen = Screen.Register },
                )
            }
        }
    }
}
