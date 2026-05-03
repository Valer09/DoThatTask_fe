package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.User
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupSummary
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.CategoryApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.ColoredDropdown
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.CreateTaskDialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.TaskCard
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.TaskDetailDialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.ToastMessage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.UpdateTaskDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val ANY_OPTION = "Anyone"
private const val ANY_CATEGORY = "Any"
private const val CREATOR_ME_OPTION = "Me"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun TaskManagementPage() {
    val taskApi = remember { TaskApi(client()) }
    val categoryApi = remember { CategoryApi(client()) }
    val scope = rememberCoroutineScope()

    val groups: List<GroupSummary> = AuthState.groups
    var selectedGroup by remember { mutableStateOf<GroupSummary?>(AuthState.activeGroup() ?: groups.firstOrNull()) }

    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    var availableCategories by remember { mutableStateOf<List<TaskCategory>>(TaskCategory.Defaults) }

    /** null = "Anyone" / "Any". */
    var creator by remember { mutableStateOf<String?>(null) }
    var category by remember { mutableStateOf<TaskCategory?>(null) }
    var assignee by remember { mutableStateOf<User?>(null) }

    var groupExpanded by remember { mutableStateOf(false) }
    var creatorExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var assigneeExpanded by remember { mutableStateOf(false) }

    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var membersLoading by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }

    var currentTaskToUpdate by remember { mutableStateOf<Task?>(null) }
    var currentDetailTask by remember { mutableStateOf<Task?>(null) }
    var taskCreationOpen by remember { mutableStateOf(false) }

    suspend fun loadMembers(groupId: Int) {
        membersLoading = true
        try {
            when (val res = taskApi.getAllUsers(groupId)) {
                is ApiResult.Success -> members = res.data
                is ApiResult.Error -> if (!res.routeIfNetwork()) {
                    toastIsError = true
                    toastMessage = res.message
                }
                else -> {}
            }
        } finally {
            membersLoading = false
        }
    }

    suspend fun runSearch() {
        val gid = selectedGroup?.id ?: return
        loading = true
        try {
            // Translate "Me" creator selection into the actual username — the
            // backend doesn't know about the synthetic option.
            val creatorParam = when (creator) {
                CREATOR_ME_OPTION -> AuthState.username
                null -> null
                else -> creator
            }
            when (val res = taskApi.searchTasks(gid, creatorParam, category, assignee?.username)) {
                is ApiResult.Success -> tasks = res.data
                is ApiResult.Error -> if (!res.routeIfNetwork()) {
                    toastIsError = true
                    toastMessage = res.message
                }
                else -> {}
            }
        } finally {
            loading = false
        }
    }

    LaunchedEffect(selectedGroup?.id) {
        val gid = selectedGroup?.id
        if (gid != null) {
            // Keep the global active-group in sync so other pages match.
            AuthState.activeGroupId = gid
            tasks = emptyList()
            creator = null
            assignee = null
            category = null
            loadMembers(gid)
            when (val res = categoryApi.list(gid)) {
                is ApiResult.Success -> availableCategories = res.data
                else -> availableCategories = TaskCategory.Defaults
            }
        }
    }

    if (taskCreationOpen) {
        CreateTaskDialog(
            groups = groups,
            initialGroupId = selectedGroup?.id,
            onConfirm = { created ->
                taskCreationOpen = false
                // Toast lives on the parent: the dialog dismisses itself
                // immediately, so a toast set inside it would never paint.
                toastIsError = false
                toastMessage = "Task '${created.name}' created"
                scope.launch { runSearch() }
            },
            onDismiss = { taskCreationOpen = false },
        )
    }

    if (currentTaskToUpdate != null) {
        UpdateTaskDialog(
            currentTaskToUpdate!!,
            onConfirm = { updated ->
                currentTaskToUpdate = null
                toastIsError = false
                toastMessage = "Task '${updated.name}' updated"
                scope.launch { runSearch() }
            },
            onDismiss = { currentTaskToUpdate = null },
        )
    }

    if (currentDetailTask != null) {
        TaskDetailDialog(
            currentDetailTask!!,
            onConfirm = {},
            onDismiss = { currentDetailTask = null },
        )
    }

    Box {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(8.dp))

            toastMessage?.let {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = (-20).dp), verticalAlignment = Alignment.Top) {
                    ToastMessage(message = it, isError = toastIsError, onDismiss = { toastMessage = null })
                }
            }

            if (groups.isEmpty()) {
                Text(
                    "You don't belong to any group yet. Create or join one to manage tasks.",
                    color = Color.Gray,
                )
                return@Column
            }

            val groups = AuthState.groups

            ColoredDropdown(
                items = groups,
                selected = selectedGroup ?: groups.first(),
                label = "Group",
                itemLabel = { it.name },
                onSelect = { selectedGroup = it ; },
                itemColor = { TaskUIHelper.parseHexColor(it.color) },
            )

            val creatorOptions: List<String> = buildList {
                add(ANY_OPTION)
                add(CREATOR_ME_OPTION)
                addAll(members.map { it.username })
            }

            ColoredDropdown(
                items = creatorOptions,
                selected = creator ?: ANY_OPTION,
                label = "Creator",
                itemLabel = { it },
                onSelect = { creator = if (it == ANY_OPTION) null else it; },
            )


            val anyCategory = TaskCategory(id = -1, name = ANY_CATEGORY)
            val categoryOptions = buildList {
                add(anyCategory)
                addAll(availableCategories)
            }
            val color =MaterialTheme.colorScheme.onSurface

            ColoredDropdown(
                items = categoryOptions,
                selected = category ?: anyCategory,
                label = "Category",
                itemLabel = { it.name },
                itemColor = { if(it.name == ANY_CATEGORY) color else TaskUIHelper.pickColor(it) },
                onSelect = { selected -> category = if (selected.id < 0) null else selected
                },
            )

            val anyUser= User("any", "")
            val userOptions = buildList {
                add(anyUser)
                addAll(members)
            }

            ColoredDropdown(
                items = userOptions,
                selected = assignee ?: anyUser,
                label = "Assignee",
                itemLabel = { it.name },
                itemColor = { color },
                onSelect = { assignee = it },
            )


            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskUIHelper.getComplementary(),
                        contentColor = Color.Black,
                    ),
                    onClick = { scope.launch { runSearch() } },
                    modifier = Modifier
                        .weight(1f)
                        .pointerHoverIcon(PointerIcon.Hand, true),
                ) { Text("Search") }
                Spacer(Modifier. width(8.dp))
                Button(
                    onClick = { taskCreationOpen = true },
                    modifier = Modifier
                        .weight(1f)
                        .pointerHoverIcon(PointerIcon.Hand, true),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskUIHelper.getSecondary(),
                        contentColor = TaskUIHelper.getAlternativeText(),
                    ),
                ) { Text("Create Task") }
            }

            Spacer(Modifier.height(8.dp))

            // Defensive client-side filter: never show tasks assigned to me.
            val visible = tasks.filter { !it.ownership_username.equals(AuthState.username, ignoreCase = true) }
            if (visible.isEmpty() && !loading) {
                Text("No tasks match the current filters.", color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(visible) { task ->
                        TaskCard(
                            task,
                            onDelete = {
                                scope.launch {
                                    when (val result = taskApi.removeTask(it)) {
                                        is ApiResult.Success -> {
                                            toastIsError = false
                                            toastMessage = result.message
                                            runSearch()
                                        }
                                        is ApiResult.Error -> if (!result.routeIfNetwork()) {
                                            toastIsError = true
                                            toastMessage = result.message
                                        }
                                        is ApiResult.NotFound -> {
                                            toastIsError = true
                                            toastMessage = result.message
                                        }
                                        is ApiResult.Unauthorized -> {
                                            toastIsError = true
                                            toastMessage = "Unauthorized"
                                            AppState.currentScreen = Screen.Login
                                        }
                                    }
                                }
                            },
                            onUpdate = { currentTaskToUpdate = task },
                            onDetails = { currentDetailTask = task },
                            onUnassign = {
                                scope.launch {
                                    val result = taskApi.unassignTask(task)
                                    if (result is ApiResult.Error && !result.routeIfNetwork()) {
                                        toastIsError = true
                                        toastMessage = result.message
                                    } else if (result is ApiResult.Success) {
                                        toastIsError = false
                                        toastMessage = "Task unassigned"
                                    }
                                    runSearch()
                                }
                            },
                        )
                    }
                }
            }
        }
        LoadingOverlay(isLoading = loading || membersLoading)
    }
}
