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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskStatus
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.User
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupSummary
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.CategoryApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper
import kotlinx.coroutines.launch




/**
 * Multi-group create dialog. The user picks a group first; the assignee
 * dropdown is then populated with that group's members. The selected group's
 * id rides on the create-task request via X-Group-Id (set by [TaskApi]).
 *
 * Success toasts are surfaced by the parent — this dialog dismisses itself
 * the instant `onConfirm` fires, so any toast set here would never paint.
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
                is ApiResult.Error -> if (!result.routeIfNetwork()) {
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
                            modifier = Modifier.fillMaxWidth().background(TaskUIHelper.getPrimary())
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        )
                        {
                            Text("Create new task", fontSize = 20.sp, color = Color.White)
                        }

                        Column(modifier = Modifier.padding(10.dp)) {
                            // Group picker first — feeds the assignee dropdown below.
                            if (groups.isNotEmpty() && selectedGroup != null) {
                                ColoredDropdown(
                                    items = groups,
                                    selected = selectedGroup ?: groups.first(),
                                    label = "Group",
                                    itemLabel = { it.name },
                                    onSelect = { selectedGroup = it },
                                    itemColor = { TaskUIHelper.parseHexColor(it.color) },
                                )
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

                            if (availableCategories.isNotEmpty()) {
                                ColoredDropdown(
                                    items = availableCategories,
                                    selected = category,
                                    label = "Category",
                                    itemLabel = { it.name },
                                    itemColor = { TaskUIHelper.pickColor(it) },
                                    onSelect = { category = it },
                                )
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
                                                is ApiResult.Success -> onConfirm(result.data)
                                                is ApiResult.Error -> if (!result.routeIfNetwork()) {
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
