package homeaq.dothattask.dothattask_fe.dothattask_fe.View


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.GroupApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.SideMenu
import org.jetbrains.compose.ui.tooling.preview.Preview




private val AppColorScheme = darkColorScheme(
    background = Color(0xff1F0A35),
    surface = Color(0xFF2B2140),
    surfaceVariant = Color(0xff2D1148),
    primary = Color(0xff7B2FBE),
    onPrimary = Color(0xffF0E6FF),
    onBackground = Color(0xffF0E6FF),
    onSurface = Color(0xffF0E6FF),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content
    )
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview

fun App(onLoginSuccess: () -> Unit = {}) {

    var isLogged by remember { mutableStateOf<Boolean?>(null) }
    val notificationTarget = remember { AppState.currentScreen.takeIf { it != Screen.Login } }

    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background) {
            LaunchedEffect(Unit) {
                AuthState.onSessionExpired = {
                    AuthState.clear()
                    AppState.currentScreen = Screen.Login
                    isLogged = false
                }

                AuthState.loadFromStorage()
                isLogged = if (AuthState.accessToken != null) {
                    val response = TaskApi(client()).checkLogin()
                    if (response is ApiResult.Success) {
                        val groupsResult = GroupApi(client()).myGroups()
                        if (groupsResult is ApiResult.Error)
                        {
                            AppState.routeToError(groupsResult.message)
                            true
                        }
                        else {
                            val groups = if (groupsResult is ApiResult.Success) groupsResult.data else emptyList()
                            AuthState.groups = groups.map { GroupSummary(it.id, it.name, it.color) }
                            if (AuthState.activeGroupId == null || AuthState.groups.none { it.id == AuthState.activeGroupId }) {
                                AuthState.activeGroupId = AuthState.groups.firstOrNull()?.id
                            }

                            AppState.currentScreen = notificationTarget
                                ?: if (AuthState.groups.isNotEmpty()) Screen.Home else Screen.NoGroup
                            true
                        }
                    } else if(response is ApiResult.Unauthorized){
                        if (AuthState.accessToken == null)
                        {
                            AppState.currentScreen = Screen.Login
                            false
                        }
                        else
                        {
                            AppState.currentScreen = notificationTarget ?: if (AuthState.groups.isNotEmpty()) Screen.Home else Screen.NoGroup
                            true
                        }
                    }
                    else if (response is ApiResult.Error)
                    {
                        AppState.routeToError(response.message)
                        true
                    }
                    else{
                        AppState.currentScreen = notificationTarget ?: if (AuthState.groups.isNotEmpty()) Screen.Home else Screen.NoGroup
                        true
                    }
                } else {
                    AppState.currentScreen = Screen.Login
                    false
                }
            }

            when (isLogged) {
                null -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                false -> {
                    when (AppState.currentScreen) {
                        Screen.Register -> RegisterPage(
                            onRegisterSuccess = {
                                AppState.currentScreen =
                                    if (AuthState.groups.isNotEmpty()) Screen.Home else Screen.NoGroup
                                isLogged = true
                                onLoginSuccess()
                            },
                        )
                        else -> LoginPage(
                            onLoginSuccess = {
                                AppState.changePage(if (AuthState.groups.isNotEmpty()) Screen.Home else Screen.NoGroup)
                                isLogged = true
                                onLoginSuccess()
                            },
                        )
                    }
                }

                true -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        SideMenu(
                            onLogout = {
                                AuthState.clear()
                                AppState.currentScreen = Screen.Login
                                isLogged = false
                            },
                            onPageChange = { AppState.currentScreen = it },
                        )
                    }
                }
            }
        }
    }

}
