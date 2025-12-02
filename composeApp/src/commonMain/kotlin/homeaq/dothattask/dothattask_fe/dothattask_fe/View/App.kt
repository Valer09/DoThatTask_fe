package homeaq.dothattask.dothattask_fe.dothattask_fe.View


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier

import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.SideMenu
import org.jetbrains.compose.ui.tooling.preview.Preview



@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview

fun App() {
    var isLogged by remember { mutableStateOf(false) }
    var selectedPage by remember { mutableStateOf<Screen>(Screen.Home) }

    if (!isLogged) {
        LoginPage(onLoginSuccess = {
            AppState.currentScreen = Screen.Home
            isLogged = AuthState.isLoggedIn
        })
    } else {


            // Menu laterale e contenuto principale
            Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(), verticalAlignment = Alignment.Top)
            {
                SideMenu({AuthState.clear(); isLogged = false}, {AppState.currentScreen = it; selectedPage = it})
            }
        }
}
