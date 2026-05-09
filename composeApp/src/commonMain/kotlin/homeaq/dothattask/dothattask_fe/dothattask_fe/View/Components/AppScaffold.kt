package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.AppLocale
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocalStrings
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocaleManager
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.localizedTitle
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState

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
    var settingsMenuExpanded by remember { mutableStateOf(false) }
    var languageMenuExpanded by remember { mutableStateOf(false) }
    val strings = LocalStrings.current


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = 35.dp, bottom = 0.dp)
                    .padding(horizontal = 40.dp),
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

                Box {
                    IconButton(
                        onClick = { settingsMenuExpanded = true },
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = AuthState.username?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = settingsMenuExpanded,
                        onDismissRequest = { settingsMenuExpanded = false },
                        modifier = Modifier.background(TaskUIHelper.getPrimary()),
                    ) {
                        DropdownMenuItem(
                            text = {
                                val flag = if (LocaleManager.current == AppLocale.Italian) "🇮🇹" else "🇬🇧"
                                Text("$flag  ${strings.headerLanguage}")
                            },
                            onClick = {
                                settingsMenuExpanded = false
                                languageMenuExpanded = true
                            },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true)
                                .background(TaskUIHelper.getSurface()).height(60.dp).width(220.dp).padding(bottom = 1.dp),
                        )
                        DropdownMenuItem(
                            text = { Text(strings.headerChangePassword) },
                            onClick = {
                                settingsMenuExpanded = false
                                AppState.changePage(Screen.ChangePassword)
                            },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true)
                                .background(TaskUIHelper.getSurface()).height(60.dp).width(220.dp).padding(bottom = 1.dp),
                        )
                        DropdownMenuItem(
                            text = { Text(strings.headerLogout) },
                            onClick = {
                                settingsMenuExpanded = false
                                scope.launch {
                                    runCatching { authApi.logout(); onLogout()}
                                }
                            },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true).background(TaskUIHelper.getSurface()).height(60.dp).width(220.dp),
                        )
                    }

                    DropdownMenu(
                        expanded = languageMenuExpanded,
                        onDismissRequest = { languageMenuExpanded = false },
                        modifier = Modifier.background(TaskUIHelper.getPrimary()),
                    ) {
                        DropdownMenuItem(
                            text = { Text("🇬🇧  ${strings.headerLanguageEnglish}") },
                            onClick = {
                                LocaleManager.set(AppLocale.English)
                                languageMenuExpanded = false
                            },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true)
                                .background(TaskUIHelper.getSurface()).height(60.dp).width(220.dp).padding(bottom = 1.dp),
                        )
                        DropdownMenuItem(
                            text = { Text("🇮🇹  ${strings.headerLanguageItalian}") },
                            onClick = {
                                LocaleManager.set(AppLocale.Italian)
                                languageMenuExpanded = false
                            },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true)
                                .background(TaskUIHelper.getSurface()).height(60.dp).width(220.dp),
                        )
                    }
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
