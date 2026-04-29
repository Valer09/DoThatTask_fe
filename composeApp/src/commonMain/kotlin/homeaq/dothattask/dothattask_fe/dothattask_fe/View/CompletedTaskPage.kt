package homeaq.dothattask.dothattask_fe.dothattask_fe.View


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupSummary
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.TaskCard
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.TaskDetailDialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.ToastMessage

private const val ANY_GROUP_LABEL = "Any"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedTaskPage() {

    val taskApi = remember { TaskApi(createHttpClient()) }
    // Server-side, /api/tasks/completed already aggregates across every
    // group the user belongs to. We fetch once and filter client-side on
    // task.groupId — fanning out per group would have returned the same
    // list N times (the endpoint ignores X-Group-Id).
    var allTasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentDetailTask by remember { mutableStateOf<Task?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    // null = "Any" → show every completed task. Otherwise, show only tasks
    // belonging to that group.
    var selectedFilter by remember { mutableStateOf<GroupSummary?>(null) }
    var groupExpanded by remember { mutableStateOf(false) }

    val groups = AuthState.groups

    LaunchedEffect(Unit) {
        loading = true
        errorMessage = null
        try {
            when (val res = taskApi.getCompleted()) {
                is ApiResult.Success -> allTasks = res.data
                is ApiResult.Error -> {
                    toastIsError = true
                    toastMessage = res.message
                }
                else -> {}
            }
        } catch (e: Exception) {
            errorMessage = "Error loading tasks: ${e.message}"
        } finally {
            loading = false
        }
    }

    val visibleTasks = selectedFilter?.let { f -> allTasks.filter { it.groupId == f.id } } ?: allTasks

    if (currentDetailTask != null) {
        TaskDetailDialog(
            currentDetailTask!!,
            onConfirm = {},
            onDismiss = { currentDetailTask = null }
        )
    }

    Box {
        Column {
            // Group filter — defaults to "Any" so the page shows every
            // completed task at first open. Switching the dropdown filters
            // the already-loaded list, no extra request needed.
            if (groups.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    ExposedDropdownMenuBox(
                        expanded = groupExpanded,
                        onExpandedChange = { groupExpanded = !groupExpanded },
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                    ) {
                        TextField(
                            value = selectedFilter?.name ?: ANY_GROUP_LABEL,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = TaskUIHelper.parseHexColor(selectedFilter?.color),
                                unfocusedTextColor = TaskUIHelper.parseHexColor(selectedFilter?.color),
                            ),
                            onValueChange = {},
                            label = { Text("Group", fontSize = 11.sp) },
                            textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
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
                        ) {
                            DropdownMenuItem(
                                text = { Text(ANY_GROUP_LABEL, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedFilter = null
                                    groupExpanded = false
                                },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                            )
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
                                        selectedFilter = g
                                        groupExpanded = false
                                    },
                                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                                )
                            }
                        }
                    }
                }
            }

            toastMessage?.let {
                ToastMessage(
                    message = it,
                    isError = toastIsError,
                    onDismiss = { toastMessage = null }
                )
            }

            if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (visibleTasks.isEmpty() && !loading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "No tasks completed yet", style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        fontSize = 30.sp,
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(visibleTasks) { task ->
                        TaskCard(
                            task,
                            onDelete = {},
                            onUpdate = { },
                            onDetails = { currentDetailTask = task },
                            hideDelete = true,
                            hideUpdate = true,
                            onUnassign = {}
                        )
                    }
                }
            }
        }
        LoadingOverlay(isLoading = loading)
    }
}
