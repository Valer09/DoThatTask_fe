package homeaq.dothattask.dothattask_fe.dothattask_fe.Model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Screen {
    Login,
    Register,
    ChangePassword,
    NoGroup,
    GroupHome,
    IncomingInvites,
    InviteMember,
    Home,
    TaskManagement,
    CompletedTask,
    Error,
}

object AppState {
    var currentScreen by mutableStateOf(Screen.Login)
    var inviteTargetGroupId by mutableStateOf<Int?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    fun routeToError(message: String?) {
        errorMessage = message
        currentScreen = Screen.Error
    }
}

