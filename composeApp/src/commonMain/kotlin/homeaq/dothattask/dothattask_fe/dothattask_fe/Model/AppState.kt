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

    /**
     * Generic full-screen fallback for unexpected failures (network down,
     * server 5xx, parsing errors, …) that aren't an auth problem.
     * Auth failures keep going through the existing `Screen.Login` path
     * triggered by [AuthState.onSessionExpired].
     */
    Error,
}

object AppState {
    var currentScreen by mutableStateOf(Screen.Login)

    /**
     * Group the user is about to send an invite for. Set when the user clicks
     * "Invite member" on a specific group card so [InviteMemberPage] knows
     * which group the invite belongs to.
     */
    var inviteTargetGroupId by mutableStateOf<Int?>(null)

    /**
     * Human-readable explanation displayed by `ErrorPage`. Set together with
     * `currentScreen = Screen.Error` (typically via [routeToError]). Cleared
     * when the user dismisses the page.
     */
    var errorMessage by mutableStateOf<String?>(null)

    /**
     * Switch to the generic error page. Use for unexpected failures only —
     * auth failures must stay on the existing login redirect path.
     */
    fun routeToError(message: String?) {
        errorMessage = message
        currentScreen = Screen.Error
    }
}

