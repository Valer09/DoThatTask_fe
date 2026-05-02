package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideMenu(onLogout: () -> Unit, onPageChange: (Screen) -> Unit) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var isHovered by remember { mutableStateOf(false) }
    val authApi = remember { AuthApi(createUnauthenticatedClient(), client()) }

    fun getRowColor(page: Screen): Color {
        if (AppState.currentScreen == page) return TaskUIHelper.getLightGray()
        return TaskUIHelper.getMarinerBlue()
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TaskUIHelper.getMarinerBlue())
                .padding(top = 35.dp, bottom = 10.dp).padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {
                scope.launch {
                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                }
            }) {
                Text("☰", color = Color.White, fontSize = 20.sp)
            }

            Text(
                text = "Welcome in DO THAT TASK, ${AuthState.displayName ?: AuthState.username ?: ""}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp),
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                onClick = {
                    // Revoke the refresh token server-side, then clear locally.
                    // Even if the network call fails we still drop local state.
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
        Row {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier
                            .width(250.dp)
                            .fillMaxHeight()
                            .background(TaskUIHelper.getMarinerBlue()),
                        drawerShape = RectangleShape,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(TaskUIHelper.getMarinerBlue())
                                .padding(start = 0.dp),
                        ) {
                            Text(
                                "Menu",
                                fontSize = 20.sp,
                                color = Color.White,
                                modifier = Modifier.padding(start = 10.dp, bottom = 16.dp),
                            )

                            val rowMod = Modifier
                                .padding(vertical = 4.dp, horizontal = 0.dp)
                                .background(if (isHovered) Color.LightGray else Color.Transparent)
                                .clickable { }
                                .pointerHoverIcon(PointerIcon.Hand, true)

                            Row(modifier = rowMod.background(getRowColor(Screen.Home))) {
                                DrawerItem("My Tasks", {
                                    scope.launch {
                                        AppState.currentScreen = Screen.Home
                                        onPageChange(Screen.Home)
                                        drawerState.close()
                                    }
                                }, Color.Black)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = rowMod.background(getRowColor(Screen.TaskManagement))) {
                                DrawerItem("Manage Tasks", {
                                    scope.launch {
                                        AppState.currentScreen = Screen.TaskManagement
                                        onPageChange(Screen.TaskManagement)
                                        drawerState.close()
                                    }
                                }, Color.Black)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = rowMod.background(getRowColor(Screen.CompletedTask))) {
                                DrawerItem("Completed Tasks", {
                                    scope.launch {
                                        AppState.currentScreen = Screen.CompletedTask
                                        onPageChange(Screen.CompletedTask)
                                        drawerState.close()
                                    }
                                }, Color.Black)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = rowMod.background(getRowColor(Screen.GroupHome))) {
                                DrawerItem("Groups", {
                                    scope.launch {
                                        val target = if (AuthState.groups.isNotEmpty()) Screen.GroupHome else Screen.NoGroup
                                        AppState.currentScreen = target
                                        onPageChange(target)
                                        drawerState.close()
                                    }
                                }, Color.Black)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = rowMod.background(getRowColor(Screen.IncomingInvites))) {
                                DrawerItem("Invites", {
                                    scope.launch {
                                        AppState.currentScreen = Screen.IncomingInvites
                                        onPageChange(Screen.IncomingInvites)
                                        drawerState.close()
                                    }
                                }, Color.Black)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = rowMod.background(getRowColor(Screen.ChangePassword))) {
                                DrawerItem("Change password", {
                                    scope.launch {
                                        AppState.currentScreen = Screen.ChangePassword
                                        onPageChange(Screen.ChangePassword)
                                        drawerState.close()
                                    }
                                }, Color.Black)
                            }
                        }
                    }
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        Box(
                            Modifier.fillMaxSize().fillMaxWidth().padding(10.dp),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            when (AppState.currentScreen) {
                                Screen.Home -> MainPage()
                                Screen.TaskManagement -> TaskManagementPage()
                                Screen.CompletedTask -> CompletedTaskPage()
                                Screen.ChangePassword -> ChangePasswordPage(
                                    onBack = { AppState.currentScreen = Screen.Home },
                                    onPasswordChanged = { AppState.currentScreen = Screen.Home },
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
                }
            }
        }
    }
}

@Composable
fun DrawerItem(label: String, onClick: () -> Unit, color: Color) {
    Text(
        text = label,
        fontSize = 18.sp,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, top = 6.dp, bottom = 6.dp)
            .clickable { onClick() },
    )
}
