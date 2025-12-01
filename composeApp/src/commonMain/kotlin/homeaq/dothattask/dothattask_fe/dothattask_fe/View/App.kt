package homeaq.dothattask.dothattask_fe.dothattask_fe.View


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import org.jetbrains.compose.ui.tooling.preview.Preview



@Composable
@Preview
fun App() {
    var isLogged by remember { mutableStateOf(false) }

    if (!isLogged)
    {
        LoginPage (onLoginSuccess =
            { AppState.currentScreen = Screen.Main; isLogged = AuthState.isLoggedIn }
        )
    }
    else
    {
        Column()
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TaskUIHelper.getMarinerBlue())   // Blu "material-ish"
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Benvenuto, ${AuthState.username}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 16.dp)
                )

                Button(
                    onClick = { AuthState.clear(); isLogged = false},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1976D2)
                    )
                ) {
                    Text("Logout")
                }
            }
            MainPage()
        }
    }

}