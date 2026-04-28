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
}

object AppState {
    var currentScreen by mutableStateOf(Screen.Login)

    /**
     * Group the user is about to send an invite for. Set when the user clicks
     * "Invite member" on a specific group card so [InviteMemberPage] knows
     * which group the invite belongs to.
     */
    var inviteTargetGroupId by mutableStateOf<Int?>(null)
}
