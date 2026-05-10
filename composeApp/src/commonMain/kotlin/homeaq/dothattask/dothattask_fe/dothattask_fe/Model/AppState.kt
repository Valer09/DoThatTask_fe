package homeaq.dothattask.dothattask_fe.dothattask_fe.Model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen.*
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocalStrings

enum class Screen {
    Onboarding,
    Login,
    Register,
    Settings,
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
    /**
     * Optional override for the page title. Most pages should leave this
     * `null` and let [Screen.localizedTitle] derive the title from
     * [LocalStrings] so it follows the active locale. Set this only for
     * dynamic titles (e.g. a specific group name) — and prefer to clear it
     * back to `null` on navigation away.
     */
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
        // Default titles are derived at render time from the active locale
        // (see [Screen.localizedTitle]); we only clear any prior override.
        title = null
    }
}

/**
 * Localised title for the page header. Reads [LocalStrings.current] so it
 * recomposes when the user changes language at runtime.
 */
@Composable
fun Screen.localizedTitle(): String {
    val s = LocalStrings.current
    return when (this) {
        Onboarding, Error -> ""
        Login -> s.titleLogin
        Register -> s.titleRegister
        Settings -> s.titleSettings
        ChangePassword -> s.titleChangePassword
        NoGroup -> ""
        GroupHome -> s.titleGroupHome
        IncomingInvites -> s.titleInvitations
        InviteMember -> s.titleInviteMember
        Home -> s.titleHome
        TaskManagement -> s.titleManageTasks
        CompletedTask -> s.titleCompleted
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
    Screen.Settings,
    Screen.ChangePassword,
    Screen.Error,
    Screen.Onboarding,
    Screen.Login,
    Screen.Register -> null
}

