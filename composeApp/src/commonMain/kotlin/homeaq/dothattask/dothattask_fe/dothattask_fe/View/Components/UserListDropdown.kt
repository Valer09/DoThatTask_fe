package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.User
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListDropdown(
    label: String,
    isLoading: (Boolean) -> Unit,
    selectedUsername: String?,
    onUserSelected: (User) -> Unit,
    onLoad: (User) -> Unit,
    height: Dp = 60.dp,
    labelSize: TextUnit = 11.sp,
    fontSizea: TextUnit = 13.sp
)
{
    val taskApi = remember { TaskApi(createHttpClient()) }
    var userList by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var userDropdownExpanded by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }

    suspend fun loadUsers() {
        try {
            val usersResult = taskApi.getAllUsers()
            if (usersResult is ApiResult.Success)
            {
                userList = usersResult.data
                if (userList.isNotEmpty()) selectedUser = selectedUsername?.let { userList.find { it.username == selectedUsername } } ?: userList.firstOrNull()

            } else if (usersResult is ApiResult.Error) {
                toastIsError = true
                toastMessage = usersResult.message
            }
        } catch (e: Exception) {
            toastIsError = true
            toastMessage = "Failed to load users: ${e.message}"
        }
        finally
        {
            isLoading(false)
            loaded = true
        }
    }

    Row(modifier = Modifier.fillMaxWidth())
    {

        LaunchedEffect(Unit)
        {
            isLoading(true)
            loadUsers()
            selectedUser?.let { onLoad(it) }
        }
        ExposedDropdownMenuBox(
            expanded = userDropdownExpanded,
            onExpandedChange = { userDropdownExpanded = !userDropdownExpanded },
            modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true)) {
            TextField(
                value = if(!loaded) "Loading. . ." else { selectedUser?.name ?: "" },
                onValueChange = {},
                label = {Text("User", fontSize = labelSize) },
                textStyle = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSizea
                ),
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .pointerHoverIcon(PointerIcon.Hand, true)
                    .fillMaxWidth().height(height)

            )
            val pointerHoverIcon = Modifier.pointerHoverIcon(PointerIcon.Hand, true)
            ExposedDropdownMenu(
                expanded = userDropdownExpanded,
                onDismissRequest = { userDropdownExpanded = false },
                modifier = pointerHoverIcon.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            ) {
                userList.forEach { user ->
                    DropdownMenuItem(
                        text = { Text(user.name, fontSize = fontSizea) },
                        onClick = {
                            selectedUser = user
                            userDropdownExpanded = false
                            onUserSelected(user)
                        },
                        modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true).height(height)
                    )
                }
            }
        }

    }
}