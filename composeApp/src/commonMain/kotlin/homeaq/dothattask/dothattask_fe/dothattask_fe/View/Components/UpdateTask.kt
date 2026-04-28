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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskStatus
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.User
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTaskDialog(
    task: Task,
    onConfirm: (Task) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(task.name) }
    var description by remember { mutableStateOf(task.description) }
    var category by remember { mutableStateOf(task.category) }
    val taskStatus = task.status
    var categoryExpanded by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    val scope = rememberCoroutineScope()

    val taskApi = remember { TaskApi(createHttpClient()) }
    var loading by remember { mutableStateOf(false) }
    var isUsersLoading by remember { mutableStateOf(true) }
    var members by remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(task.groupId) {
        isUsersLoading = true
        try {
            when (val res = taskApi.getAllUsers(task.groupId)) {
                is ApiResult.Success -> {
                    members = res.data
                    selectedUser = members.firstOrNull { it.username == task.ownership_username }
                        ?: members.firstOrNull()
                }
                is ApiResult.Error -> {
                    toastIsError = true
                    toastMessage = res.message
                }
                else -> {}
            }
        } finally {
            isUsersLoading = false
        }
    }

    val colors = TextFieldDefaults.colors(
        focusedTextColor = Color.Blue,
        focusedContainerColor = TaskUIHelper.getLightGray(),
        unfocusedContainerColor = TaskUIHelper.getGray(),
    )

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
                        modifier = Modifier.fillMaxWidth().background(TaskUIHelper.getMarinerBlue()).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    )
                    {
                        Text("Update ${task.name}", fontSize = 20.sp, color = Color.White)
                    }

                    Column(modifier = Modifier.padding(10.dp)) {
                        if (task.groupName.isNotBlank()) {
                            GroupBadge(task.groupName, task.groupColor)
                            Spacer(Modifier.height(10.dp))
                        }
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
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true)
                        ) {
                            TextField(
                                value = category.name,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = TaskUIHelper.pickColor(category),
                                    unfocusedTextColor = TaskUIHelper.pickColor(category),
                                ),
                                onValueChange = {},
                                label = { Text("Category") },
                                textStyle = TextStyle(fontWeight = FontWeight.Bold),
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
                                TaskCategory.entries.forEach { cat ->
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
                            selectedUsername = selectedUser?.username ?: task.ownership_username,
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
                                    loading = true
                                    val newTask = Task(
                                        name = name,
                                        description = description,
                                        category = category,
                                        status = TaskStatus.valueOf(taskStatus.name),
                                        ownership_username = selectedUser?.username ?: task.ownership_username,
                                        groupId = task.groupId,
                                        groupName = task.groupName,
                                        groupColor = task.groupColor,
                                    )
                                    scope.launch {
                                        when (val result = taskApi.updateTask(task, newTask)) {
                                            is ApiResult.Success -> {
                                                toastIsError = false
                                                toastMessage = "Completed"
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
                                Text("Update")
                            }
                        }
                    }

                }
            }
        }
    }
}
