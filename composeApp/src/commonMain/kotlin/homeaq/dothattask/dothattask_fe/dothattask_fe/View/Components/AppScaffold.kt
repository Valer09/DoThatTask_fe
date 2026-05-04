package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createUnauthenticatedClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.ChangePasswordPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.CompletedTaskPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.ErrorPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.GroupHomePage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.IncomingInvitesPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.InviteMemberPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.MainPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.NoGroupPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskManagementPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper
import kotlinx.coroutines.launch

/**
 * Root scaffold for the authenticated portion of the app. Replaces the
 * old `ModalNavigationDrawer`-based `SideMenu` with a fixed bottom
 * navigation bar (Revolut-style).
 *
 * Top bar: page title (left), Settings icon → ChangePassword,
 * Logout button (right).
 *
 * Bottom bar: [BottomNavBar] with the 5 top-level destinations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    val authApi = remember { AuthApi(createUnauthenticatedClient(), client()) }
    val headerColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(top = 35.dp, bottom = 10.dp)
                    .padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AppState.title?.let {
                    Text(
                        text = it,
                        color = onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f),
                    )
                } ?: run {
                    // Keeps the right-side controls aligned even when the
                    // title is null (e.g. NoGroup / Error screens).
                    Box(modifier = Modifier.weight(1f))
                }

                IconButton(
                    onClick = { AppState.changePage(Screen.ChangePassword) },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Change password",
                        tint = onSurface,
                    )
                }

                Button(
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                    onClick = {
                        scope.launch {
                            runCatching { authApi.logout() }
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskUIHelper.getRed(),
                        contentColor = Color.White,
                    ),
                ) {
                    Text("Logout")
                }
            }
        },
        bottomBar = { BottomNavBar() },
    ) { innerPadding ->
        AppContent(innerPadding)
    }
}

@Composable
private fun AppContent(innerPadding: PaddingValues) {
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
            Screen.ChangePassword -> ChangePasswordPage(
                onBack = { AppState.changePage(Screen.Home) },
                onPasswordChanged = { AppState.changePage(Screen.Home) },
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
