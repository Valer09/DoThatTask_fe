package homeaq.dothattask.dothattask_fe.dothattask_fe.View


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier

import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupSummary
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.GroupApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.SideMenu
import org.jetbrains.compose.ui.tooling.preview.Preview



@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview

fun App() {
    var isLogged by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        // Pull persisted tokens into memory (if any), then try to validate
        // the session by hitting /api/user/me with the bearer client. If the
        // access token is expired, the Auth plugin will transparently call
        // /api/auth/refresh.
        AuthState.loadFromStorage()
        isLogged = if (AuthState.accessToken != null) {
            val client = createHttpClient(onRefreshFailed = {
                AuthState.clear()
                AppState.currentScreen = Screen.Login
            })
            val taskApi = TaskApi(client)
            val response = taskApi.checkLogin()
            if (response is ApiResult.Success) {
                // Reload the user's groups: tokens persisted to storage don't
                // include them, and the side menu / pages depend on
                // AuthState.groups being populated.
                val groups = when (val gr = GroupApi(client).myGroups()) {
                    is ApiResult.Success -> gr.data
                    else -> emptyList()
                }
                AuthState.groups = groups.map { GroupSummary(it.id, it.name, it.color) }
                if (AuthState.activeGroupId == null || AuthState.groups.none { it.id == AuthState.activeGroupId }) {
                    AuthState.activeGroupId = AuthState.groups.firstOrNull()?.id
                }
                AppState.currentScreen =
                    if (AuthState.groups.isNotEmpty()) Screen.Home else Screen.NoGroup
                true
            } else {
                AuthState.clear()
                AppState.currentScreen = Screen.Login
                false
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
                    },
                )
                else -> LoginPage(
                    onLoginSuccess = {
                        AppState.currentScreen =
                            if (AuthState.groups.isNotEmpty()) Screen.Home else Screen.NoGroup
                        isLogged = true
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
