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

/**
 * The 5 destinations rendered in the bottom navigation bar, left → right.
 * Order is meaningful (it drives the visual tab order).
 */
val bottomNavDestinations: List<Screen> = listOf(
    Screen.Home,
    Screen.TaskManagement,
    Screen.CompletedTask,
    Screen.GroupHome,
    Screen.IncomingInvites,
)

/**
 * Maps the current screen to the tab that should appear "selected" in the
 * bottom bar. Sub-pages (InviteMember, NoGroup) bubble up to their parent
 * tab; pages that aren't part of the navigation (ChangePassword, Error,
 * Login, Register) return `null` so no tab is highlighted.
 */
fun Screen.topLevel(): Screen? = when (this) {
    Screen.Home,
    Screen.TaskManagement,
    Screen.CompletedTask,
    Screen.GroupHome,
    Screen.IncomingInvites -> this
    Screen.NoGroup,
    Screen.InviteMember -> Screen.GroupHome
    Screen.ChangePassword,
    Screen.Error,
    Screen.Login,
    Screen.Register -> null
}

