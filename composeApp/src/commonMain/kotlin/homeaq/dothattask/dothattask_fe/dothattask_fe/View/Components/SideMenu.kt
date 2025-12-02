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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.MainPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskManagementPage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideMenu(onLogout: () -> Unit, onPageChange: (Screen) -> Unit) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var isHovered by remember { mutableStateOf(false) }
    var selectedPage by remember { mutableStateOf<Screen>(Screen.Home) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TaskUIHelper.getMarinerBlue())
                .padding(top = 35.dp, bottom = 10.dp).padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona menu
            IconButton(onClick = {
                scope.launch {
                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                }
            }) {
                Text("☰", color = Color.White, fontSize = 20.sp)
            }

            // Titolo
            Text(
                text = "Welcome in DO THAT TASK, ${AuthState.username}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottone Logout
            Button(
                onClick = { onLogout() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskUIHelper.getRed(),
                    contentColor = Color.White
                )
            ) {
                Text("Logout")
            }
        }
    Row()
    {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier
                        .width(250.dp)
                        .fillMaxHeight()
                        .background(TaskUIHelper.getMarinerBlue()),
                    drawerShape = RectangleShape

                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TaskUIHelper.getMarinerBlue()) // blu "material-ish"
                            .padding( start = 0.dp, top = 10.dp)
                    ) {

                        Text("Menu", fontSize = 20.sp, color = Color.White, modifier = Modifier.padding(start = 10.dp, bottom = 16.dp))

                        val modifier = Modifier
                            .padding(vertical = 4.dp, horizontal = 0.dp)
                            .background(if (isHovered) Color.LightGray else Color.Transparent)
                            .clickable { }
                            .pointerHoverIcon(PointerIcon.Hand, true )

                        Row(modifier = modifier) {DrawerItem("Home", { scope.launch { selectedPage = Screen.Home;onPageChange(Screen.Home) }}, Color.Black)}
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = modifier) {DrawerItem("Tasks", {scope.launch { selectedPage = Screen.TaskManagement; onPageChange(Screen.TaskManagement) }}, Color.Black)}
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = modifier) {DrawerItem("Settings", {}, Color.Black)}
                    }
                }
            }
        ) {

            Box(
                Modifier.fillMaxSize()
            ) {
                Column(
                    Modifier.fillMaxSize()

                ) {
                    Box(
                        Modifier.fillMaxSize()
                            .fillMaxWidth().padding(10.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if(selectedPage == Screen.Home) MainPage()
                        if(selectedPage == Screen.TaskManagement) TaskManagementPage()
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
            .clickable { onClick() }
    )
}
