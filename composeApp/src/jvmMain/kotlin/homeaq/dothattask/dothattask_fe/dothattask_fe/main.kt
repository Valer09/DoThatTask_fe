package homeaq.dothattask.dothattask_fe.dothattask_fe

import homeaq.dothattask.dothattask_fe.dothattask_fe.View.App
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "dothattask_fe",
    ) {
        App()
    }
}

