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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthProvider
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
        val username = AuthProvider.getUsername()
        val token = AuthProvider.getToken()

        isLogged = if (username != null && token != null) {
            val taskApi = TaskApi(createHttpClient(token))
            val response = taskApi.checkLogin()
            if (response is ApiResult.Success) {
                AuthState.username = username
                AuthState.token = token
                true
            } else false
        } else false
    }

    when (isLogged) {
        null -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        false -> {
            LoginPage(
                onLoginSuccess = {
                    AppState.currentScreen = Screen.Home
                    isLogged = true
                    AuthProvider.saveUsername(AuthState.username.toString())
                    AuthProvider.saveToken(AuthState.token.toString())
                }
            )
        }
        true -> {
            AppState.currentScreen = Screen.Home
            Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(), verticalAlignment = Alignment.Top)
            {
                SideMenu({AuthState.clear(); isLogged = false; AuthProvider.cleanToken()}, {AppState.currentScreen = it})
            }
        }
    }
}

