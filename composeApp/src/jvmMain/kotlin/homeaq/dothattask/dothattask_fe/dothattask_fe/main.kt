package homeaq.dothattask.dothattask_fe.dothattask_fe

import androidx.compose.ui.unit.dp
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.App
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Do That Task!",
        state = rememberWindowState(
            width = 600.dp,
            height = 900.dp
        )
    ) {
        App()
    }
}

