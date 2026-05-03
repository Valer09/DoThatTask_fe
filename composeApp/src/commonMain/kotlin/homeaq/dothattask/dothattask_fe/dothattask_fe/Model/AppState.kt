package homeaq.dothattask.dothattask_fe.dothattask_fe.Model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen.*

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
    var title by mutableStateOf<String?>(null)
    var currentScreen by mutableStateOf(Screen.Login)
    var inviteTargetGroupId by mutableStateOf<Int?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    fun routeToError(message: String?) {
        errorMessage = message
        currentScreen = Screen.Error
    }

    fun changePage(screen: Screen) {
        currentScreen = screen
        title = when(screen) {
            Login -> "Welcome to Do That Task!"
            Register -> "Register"
            ChangePassword -> "Change password"
            NoGroup -> ""
            GroupHome -> "Current task"
            IncomingInvites -> "Invitations"
            InviteMember -> "Invite friends!"
            Home -> "Current task"
            TaskManagement -> "Manage tasks"
            CompletedTask -> "Completed tasks"
            Error -> ""
        }
    }
}

