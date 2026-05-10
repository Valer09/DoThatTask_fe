package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.localizedTitle
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.ChangePasswordPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.CompletedTaskPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.ErrorPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.GroupHomePage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.IncomingInvitesPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.InviteMemberPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.MainPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.NoGroupPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.SettingsPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskManagementPage

/**
 * Root scaffold for the authenticated portion of the app. Replaces the
 * old `ModalNavigationDrawer`-based `SideMenu` with a fixed bottom
 * navigation bar (Revolut-style).
 *
 * Top bar: page title (left, derived from [Screen.localizedTitle] so it
 * follows the active locale) + avatar (right). Tapping the avatar opens
 * the dedicated Settings page, which now hosts every account action
 * (language picker, change password, logout).
 *
 * Bottom bar: [BottomNavBar] with the 5 top-level destinations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(onLogout: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    // Lift the header above the status bar / notch on
                    // edge-to-edge devices instead of the old hardcoded
                    // 35dp top padding.
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 8.dp)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Custom override (rare — set by a page that wants a dynamic
                // title) wins; otherwise we render the localised default for
                // the current screen.
                val pageTitle = AppState.title ?: AppState.currentScreen.localizedTitle()
                if (pageTitle.isNotEmpty()) {
                    Text(
                        text = pageTitle,
                        color = onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    // Keeps the right-side controls aligned even when there
                    // is no title (NoGroup / Error screens).
                    Box(modifier = Modifier.weight(1f))
                }

                // Tap the avatar to open the dedicated Settings page —
                // replaces the old dropdown menu so all account actions
                // (language, change password, logout) live in a single
                // place that's easier to grow.
                IconButton(
                    onClick = { AppState.changePage(Screen.Settings) },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (AuthState.email ?: AuthState.username)
                                ?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                    }
                }
            }
        },
        bottomBar = { BottomNavBar() },
    ) { innerPadding ->
        AppContent(innerPadding, onLogout)
    }
}

@Composable
private fun AppContent(innerPadding: PaddingValues, onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(10.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        when (AppState.currentScreen) {
            Screen.Home -> MainPage()
            Screen.TaskManagement -> TaskManagementPage()
            Screen.CompletedTask -> CompletedTaskPage()
            Screen.Settings -> SettingsPage(onLogout = onLogout)
            // ChangePassword is reached from the Settings page now; both
            // 'back' and 'password changed' return there so the user
            // doesn't get teleported to Home mid-flow.
            Screen.ChangePassword -> ChangePasswordPage(
                onBack = { AppState.changePage(Screen.Settings) },
                onPasswordChanged = { AppState.changePage(Screen.Settings) },
            )
            Screen.NoGroup -> NoGroupPage()
            Screen.GroupHome -> GroupHomePage()
            Screen.IncomingInvites -> IncomingInvitesPage()
            Screen.InviteMember -> InviteMemberPage()
            Screen.Error -> ErrorPage()
            else -> MainPage()
        }
    }
}
