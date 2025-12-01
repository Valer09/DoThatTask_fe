package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults

import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
    var taskStatus by remember { mutableStateOf(task.status) }
    val userList = listOf("alice", "bob", "carlo")
    var assignedUser by remember { mutableStateOf(userList.first()) }
    var userExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val taskApi = remember { TaskApi(createHttpClient()) }

    val colors = TextFieldDefaults.colors(
        focusedTextColor = Color.Blue,
        focusedContainerColor = TaskUIHelper.Companion.getLightGray(),
        unfocusedContainerColor = TaskUIHelper.Companion.getGray(),
    )

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
                    modifier = Modifier.fillMaxWidth().background(TaskUIHelper.Companion.getMarinerBlue()).padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                )
                {
                    Text("Update ${task.name}", fontSize = 20.sp)
                }

                Column(modifier = Modifier.padding(10.dp)) {
                    Spacer(Modifier.height(15.dp))
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        colors = colors,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    TextField(

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
                        modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true)

                    ) {
                        TextField(
                            value = category.name,
                            colors = TextFieldDefaults.colors(focusedTextColor = TaskUIHelper.Companion.pickColor(category), unfocusedTextColor = TaskUIHelper.Companion.pickColor(category)),
                            onValueChange = {},
                            label = { Text("Category") },
                            textStyle = TextStyle( fontWeight = FontWeight.Bold),
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                .pointerHoverIcon(PointerIcon.Hand, true)

                        )

                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                        ) {
                            TaskCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name, fontWeight = FontWeight.Bold, color = TaskUIHelper.Companion.pickColor(cat)) },
                                    onClick = {
                                        category = cat
                                        categoryExpanded = false
                                    },
                                    modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = userExpanded,
                        onExpandedChange = { userExpanded = !userExpanded },
                        modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true)
                    ) {
                        TextField(
                            value = assignedUser,
                            onValueChange = {},
                            label = { Text("Assign to") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                .pointerHoverIcon(PointerIcon.Hand, true)

                        )
                        ExposedDropdownMenu(
                            expanded = userExpanded,
                            onDismissRequest = { userExpanded = false },
                            modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true)
                        ) {
                            userList.forEach { user ->
                                DropdownMenuItem(
                                    text = { Text(user) },
                                    onClick = {
                                        assignedUser = user
                                        userExpanded = false
                                    },
                                    modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween)
                    {

                        OutlinedButton(
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black, containerColor = TaskUIHelper.Companion.getGray()),
                            onClick = {onDismiss()}
                        )
                        {
                            Text("Cancel")
                        }

                        OutlinedButton(
                            modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black, containerColor = TaskUIHelper.Companion.getGreen()),
                            onClick = {
                                val newTask = Task(
                                    task.name,
                                    description,
                                    try {
                                        TaskCategory.valueOf(category.name)
                                    } catch (e: IllegalArgumentException) {
                                        TaskCategory.Social
                                    },
                                    TaskStatus.valueOf(taskStatus.name),
                                    task.ownership_username
                                )
                                scope.launch {
                                    when (val result = taskApi.updateTask(newTask)) {
                                        is ApiResult.Success -> {
                                            toastIsError = false
                                            toastMessage = "Completed"
                                            onConfirm(result.data)
                                        }
                                        is ApiResult.Error -> {
                                            toastIsError = true
                                            toastMessage = result.message
                                        }
                                    }
                                }

                                onConfirm(newTask)
                            })
                        {
                            Text("Update")
                        }
                    }
                }

            }
        }
    }
}

