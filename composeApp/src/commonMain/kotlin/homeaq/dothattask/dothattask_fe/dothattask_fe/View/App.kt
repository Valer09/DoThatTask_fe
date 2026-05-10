package homeaq.dothattask.dothattask_fe.dothattask_fe.View


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupSummary
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocalStrings
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocaleManager
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.stringsFor
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.GroupApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.OnboardingPreferences
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.isNetworkError
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.AppScaffold
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.clearAuthTokens
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import org.jetbrains.compose.ui.tooling.preview.Preview


private val AppColorScheme = darkColorScheme(
    background = Color(0xff1F0A35),
    surface = Color(0xFF2B2140),
    surfaceVariant = Color(0xff2D1148),
    primary = Color(0xff7B2FBE),
    onPrimary = Color(0xffF0E6FF),
    onBackground = Color(0xffF0E6FF),
    onSurface = Color(0xffF0E6FF),
    // Vivid red so the system reads "destructive / urgent" at a glance —
    // used by trash icons, the invites badge and field-level errors.
    error = Color(0xffE53935),
    onError = Color(0xffFFFFFF),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content
    )
}

private sealed class AppInitState {
    object Loading : AppInitState()
    object LoggedIn : AppInitState()
    object LoggedOut : AppInitState()
    object Error : AppInitState()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App(onLoginSuccess: () -> Unit = {}) {
    var initState by remember { mutableStateOf<AppInitState>(AppInitState.Loading) }
    val notificationTarget = remember { AppState.currentScreen.takeIf { it != Screen.Login } }
    var initKey by remember { mutableStateOf(0) }
    // Feature carousel: read once on first render. Once dismissed we set
    // both the persistent flag and this in-memory state so the recomposition
    // swaps the page out for AppScaffold without reading prefs again.
    var featuresSeen by remember { mutableStateOf(OnboardingPreferences.hasSeenFeatures()) }


    val activeLocale = LocaleManager.current
    val activeStrings = stringsFor(activeLocale)

    AppTheme {
        CompositionLocalProvider(LocalStrings provides activeStrings) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            LaunchedEffect(initKey) {
                // Catch Throwable (not just Exception) because on Kotlin/Wasm-JS
                // browser fetch failures are plain JS errors that don't extend
                // kotlin.Exception and would otherwise escape silently, leaving
                // initState = Loading forever.
                try {
                    AuthState.onSessionExpired = {
                        AuthState.clear()
                        AppState.currentScreen = Screen.Login
                        initState = AppInitState.LoggedOut
                    }
                    AuthState.loadFromStorage()

                    initState = if (AuthState.accessToken != null) {
                        val response = TaskApi(client()).checkLogin()
                        when {
                            response is ApiResult.Success -> {
                                val groupsResult = GroupApi(client()).myGroups()
                                if (groupsResult is ApiResult.Error) {
                                    AppState.routeToError(activeStrings.connectionError)
                                    AppInitState.Error
                                } else {
                                    val groups = if (groupsResult is ApiResult.Success) groupsResult.data else emptyList()
                                    AuthState.groups = groups.map { GroupSummary(it.id, it.name, it.color) }
                                    if (AuthState.activeGroupId == null || AuthState.groups.none { it.id == AuthState.activeGroupId })
                                        AuthState.activeGroupId = AuthState.groups.firstOrNull()?.id
                                    AppState.currentScreen = notificationTarget
                                        ?: if (AuthState.groups.isNotEmpty()) Screen.Home else Screen.NoGroup
                                    AppInitState.LoggedIn
                                }
                            }
                            response is ApiResult.Unauthorized -> {
                                client().clearAuthTokens()
                                AuthState.clear()
                                AppState.currentScreen =
                                    if (!OnboardingPreferences.hasSeenOnboarding()) Screen.Onboarding
                                    else Screen.Login
                                AppInitState.LoggedOut
                            }
                            response is ApiResult.Error -> {
                                AppState.routeToError(activeStrings.connectionError)
                                AppInitState.Error
                            }
                            else -> {
                                AppState.currentScreen = notificationTarget
                                    ?: if (AuthState.groups.isNotEmpty()) Screen.Home else Screen.NoGroup
                                AppInitState.LoggedIn
                            }
                        }
                    } else {
                        // First launch on this device → tutorial. Once dismissed
                        // the flag is persisted so subsequent launches go
                        // straight to login.
                        AppState.currentScreen =
                            if (!OnboardingPreferences.hasSeenOnboarding()) Screen.Onboarding
                            else Screen.Login
                        AppInitState.LoggedOut
                    }
                } catch (t: Throwable) {
                    // Genuine coroutine cancellations (navigation teardown) are
                    // re-thrown by networkError; everything else means the server
                    // was unreachable — show the error page.
                    if (t is kotlinx.coroutines.CancellationException && !isNetworkError(t)) throw t
                    AppState.routeToError(activeStrings.connectionError)
                    initState = AppInitState.Error
                }
            }

            when (initState) {
                AppInitState.Loading -> Box(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                AppInitState.Error -> ErrorPage(onRetry = {
                    initKey++
                })

                AppInitState.LoggedOut -> when (AppState.currentScreen) {
                    Screen.Onboarding -> OnboardingPage(
                        onFinish = {

                            if (AppState.currentScreen == Screen.Onboarding) {
                                AppState.changePage(Screen.Register)
                            }
                        },
                    )
                    Screen.Register -> RegisterPage(
                        onRegisterSuccess = {
                            AppState.currentScreen =
                                if (AuthState.groups.isNotEmpty()) Screen.Home else Screen.NoGroup
                            initState = AppInitState.LoggedIn
                            onLoginSuccess()
                        },
                    )
                    else -> LoginPage(
                        onLoginSuccess = {
                            AppState.changePage(if (AuthState.groups.isNotEmpty()) Screen.Home else Screen.NoGroup)
                            initState = AppInitState.LoggedIn
                            onLoginSuccess()
                        },
                    )
                }

                AppInitState.LoggedIn -> if (!featuresSeen) {
                    FeatureTourPage(
                        onFinish = {
                            OnboardingPreferences.markFeaturesSeen()
                            featuresSeen = true
                        },
                    )
                } else {
                    AppScaffold(
                        onLogout = {
                            client().clearAuthTokens()
                            AuthState.clear()
                            AppState.currentScreen = Screen.Login
                            initState = AppInitState.LoggedOut
                        },
                    )
                }
            }
        }
        }
    }
}
