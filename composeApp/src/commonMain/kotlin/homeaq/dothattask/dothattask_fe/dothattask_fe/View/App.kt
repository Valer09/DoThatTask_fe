package homeaq.dothattask.dothattask_fe.dothattask_fe.View


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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

    if (!isLogged) {
        LoginPage(onLoginSuccess = {
            AppState.currentScreen = Screen.Main
            isLogged = AuthState.isLoggedIn
        })
    } else {


            // Menu laterale e contenuto principale
            Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(), verticalAlignment = Alignment.Top) {
                SideMenu({AuthState.clear(); isLogged = false})

            }
        }
}
