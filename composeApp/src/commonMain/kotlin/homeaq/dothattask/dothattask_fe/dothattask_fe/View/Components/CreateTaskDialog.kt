package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskStatus
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.User
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupSummary
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.CategoryApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper
import kotlinx.coroutines.launch




/**
 * Multi-group create dialog. The user picks a group first; the assignee
 * dropdown is then populated with that group's members. The selected group's
 * id rides on the create-task request via X-Group-Id (set by [TaskApi]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    groups: List<GroupSummary>,
    initialGroupId: Int?,
    onConfirm: (Task) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var availableCategories by remember { mutableStateOf(TaskCategory.Defaults) }
    var category by remember { mutableStateOf(TaskCategory.Social) }
    val taskStatus = TaskStatus.TODO
    var categoryExpanded by remember { mutableStateOf(false) }
    var groupExpanded by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    val initial = remember(initialGroupId, groups) {
        groups.firstOrNull { it.id == initialGroupId } ?: groups.firstOrNull()
    }
    var selectedGroup by remember { mutableStateOf(initial) }

    val scope = rememberCoroutineScope()

    val taskApi = remember { TaskApi(client()) }
    val categoryApi = remember { CategoryApi(client()) }

    val colors = TextFieldDefaults.colors(
        focusedTextColor = Color.Blue,
        focusedContainerColor = TaskUIHelper.getLightGray(),
        unfocusedContainerColor = TaskUIHelper.getGray(),
    )

    var loading by remember { mutableStateOf(false) }
    var isUsersLoading by remember { mutableStateOf(false) }
    var members by remember { mutableStateOf<List<User>>(emptyList()) }

    // Re-fetch the assignee list AND the group's category list whenever the
    // group changes — categories are per-group now (defaults + customs).
    LaunchedEffect(selectedGroup?.id) {
        val gid = selectedGroup?.id ?: return@LaunchedEffect
        isUsersLoading = true
        members = emptyList()
        selectedUser = null
        try {
            when (val result = taskApi.getAllUsers(gid)) {
                is ApiResult.Success -> {
                    members = result.data
                    selectedUser = members.firstOrNull()
                }
                is ApiResult.Error -> {
                    toastIsError = true
                    toastMessage = result.message
                }
                else -> {}
            }
        } finally {
            isUsersLoading = false
        }
        when (val result = categoryApi.list(gid)) {
            is ApiResult.Success -> {
                if (result.data.isNotEmpty()) {
                    availableCategories = result.data
                    category = availableCategories.firstOrNull { it.id == category.id }
                        ?: availableCategories.first()
                }
            }
            else -> {}
        }
    }

    Box(modifier = Modifier
        .fillMaxSize().wrapContentSize(Alignment.Center)){

            LoadingOverlay(isLoading = loading, Color.Transparent)

            Dialog(onDismissRequest = {}) {
                Column()
                {
                    toastMessage?.let {
                        ToastMessage(
                            message = it,
                            isError = toastIsError,
                            onDismiss = { toastMessage = null }
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        shape = RoundedCornerShape(CornerSize(4.dp))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(TaskUIHelper.getMarinerBlue())
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        )
                        {
                            Text("Create new task", fontSize = 20.sp, color = Color.White)
                        }

                        Column(modifier = Modifier.padding(10.dp)) {
                            Spacer(Modifier.height(15.dp))
                            // Group picker first — feeds the assignee dropdown below.
                            ExposedDropdownMenuBox(
                                expanded = groupExpanded,
                                onExpandedChange = { groupExpanded = !groupExpanded },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                            ) {
                                TextField(
                                    value = selectedGroup?.name ?: "",
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = TaskUIHelper.parseHexColor(selectedGroup?.color),
                                        unfocusedTextColor = TaskUIHelper.parseHexColor(selectedGroup?.color),
                                    ),
                                    onValueChange = {},
                                    label = { Text("Group", fontSize = 11.sp) },
                                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                        .pointerHoverIcon(PointerIcon.Hand, true),
                                )

                                ExposedDropdownMenu(
                                    expanded = groupExpanded,
                                    onDismissRequest = { groupExpanded = false },
                                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                                ) {
                                    groups.forEach { g ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    g.name,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TaskUIHelper.parseHexColor(g.color),
                                                )
                                            },
                                            onClick = {
                                                selectedGroup = g
                                                groupExpanded = false
                                            },
                                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            TextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Name") },
                                colors = colors,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Description") },
                                colors = colors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                            )

                            Spacer(Modifier.height(10.dp))

                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = !categoryExpanded },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                            ) {
                                TextField(
                                    value = category.name,
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = TaskUIHelper.pickColor(category),
                                        unfocusedTextColor = TaskUIHelper.pickColor(category),
                                    ),
                                    onValueChange = {},
                                    label = { Text("Category", fontSize = 11.sp) },
                                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                        .pointerHoverIcon(PointerIcon.Hand, true),
                                )

                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false },
                                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                                ) {
                                    availableCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    cat.name,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TaskUIHelper.pickColor(cat),
                                                )
                                            },
                                            onClick = {
                                                category = cat
                                                categoryExpanded = false
                                            },
                                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            UserListDropdown(
                                label = "Assignee",
                                users = members,
                                isLoading = isUsersLoading,
                                selectedUsername = selectedUser?.username,
                                onUserSelected = { selectedUser = it },
                                onLoad = { selectedUser = it },
                            )

                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            )
                            {
                                OutlinedButton(
                                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.Black,
                                        containerColor = TaskUIHelper.getGray(),
                                    ),
                                    onClick = { onDismiss() },
                                )
                                {
                                    Text("Close")
                                }

                                OutlinedButton(
                                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.Black,
                                        containerColor = TaskUIHelper.getGreen(),
                                    ),
                                    onClick = {
                                        val gid = selectedGroup?.id
                                        val assignee = selectedUser?.username
                                        if (gid == null) {
                                            toastIsError = true
                                            toastMessage = "Pick a group first"
                                            return@OutlinedButton
                                        }
                                        if (assignee.isNullOrBlank()) {
                                            toastIsError = true
                                            toastMessage = "Pick an assignee first"
                                            return@OutlinedButton
                                        }
                                        val trimmedName = name.trim()
                                        if (trimmedName.isEmpty()) {
                                            toastIsError = true
                                            toastMessage = "Name cannot be empty"
                                            return@OutlinedButton
                                        }
                                        val newTask = Task(
                                            name = trimmedName,
                                            description = description.trim(),
                                            category = category,
                                            status = taskStatus,
                                            ownership_username = assignee,
                                        )
                                        scope.launch {
                                            loading = true
                                            when (val result = taskApi.createTask(newTask, gid)) {
                                                is ApiResult.Success -> {
                                                    toastIsError = false
                                                    toastMessage = "Created"
                                                    onConfirm(result.data)
                                                }
                                                is ApiResult.Error -> {
                                                    toastIsError = true
                                                    toastMessage = result.message
                                                }
                                                else -> {}
                                            }
                                            loading = false
                                        }
                                    },
                                )
                                {
                                    Text("Create")
                                }
                            }
                        }
                    }
                }
            }
    }
}
