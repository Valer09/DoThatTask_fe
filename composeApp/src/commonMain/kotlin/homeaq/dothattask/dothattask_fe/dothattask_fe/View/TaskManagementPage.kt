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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.User
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupSummary
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.CategoryApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.CreateTaskDialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.TaskCard
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.TaskDetailDialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.ToastMessage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.UpdateTaskDialog
import kotlinx.coroutines.launch

private const val ANY_OPTION = "Anyone"
private const val CREATOR_ME_OPTION = "Me"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagementPage() {
    val taskApi = remember { TaskApi(createHttpClient()) }
    val categoryApi = remember { CategoryApi(createHttpClient()) }
    val scope = rememberCoroutineScope()

    val groups: List<GroupSummary> = AuthState.groups
    var selectedGroup by remember { mutableStateOf<GroupSummary?>(AuthState.activeGroup() ?: groups.firstOrNull()) }

    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    var availableCategories by remember { mutableStateOf<List<TaskCategory>>(TaskCategory.Defaults) }

    /** null = "Anyone" / "Any". */
    var creator by remember { mutableStateOf<String?>(null) }
    var category by remember { mutableStateOf<TaskCategory?>(null) }
    var assignee by remember { mutableStateOf<String?>(null) }

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
                is ApiResult.Error -> {
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
            when (val res = taskApi.searchTasks(gid, creatorParam, category, assignee)) {
                is ApiResult.Success -> tasks = res.data
                is ApiResult.Error -> {
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
            onConfirm = {
                taskCreationOpen = false
                scope.launch { runSearch() }
            },
            onDismiss = { taskCreationOpen = false },
        )
    }

    if (currentTaskToUpdate != null) {
        UpdateTaskDialog(
            currentTaskToUpdate!!,
            onConfirm = {
                currentTaskToUpdate = null
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
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Manage Tasks", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            if (groups.isEmpty()) {
                Text(
                    "You don't belong to any group yet. Create or join one to manage tasks.",
                    color = Color.Gray,
                )
                return@Column
            }

            // Group filter
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
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp),
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
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Creator filter (Anyone / Me / one of the group members)
            val creatorOptions: List<String> = buildList {
                add(ANY_OPTION)
                add(CREATOR_ME_OPTION)
                addAll(members.map { it.username })
            }
            ExposedDropdownMenuBox(
                expanded = creatorExpanded,
                onExpandedChange = { creatorExpanded = !creatorExpanded },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
            ) {
                TextField(
                    value = creator ?: ANY_OPTION,
                    onValueChange = {},
                    label = { Text("Creator", fontSize = 11.sp) },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = creatorExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                        .pointerHoverIcon(PointerIcon.Hand, true),
                )
                ExposedDropdownMenu(
                    expanded = creatorExpanded,
                    onDismissRequest = { creatorExpanded = false },
                ) {
                    creatorOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = {
                                creator = if (opt == ANY_OPTION) null else opt
                                creatorExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Category filter
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
            ) {
                TextField(
                    value = category?.name ?: "Any",
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = category?.let { TaskUIHelper.pickColor(it) } ?: Color.Black,
                        unfocusedTextColor = category?.let { TaskUIHelper.pickColor(it) } ?: Color.Black,
                    ),
                    onValueChange = {},
                    label = { Text("Category", fontSize = 11.sp) },
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp),
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
                ) {
                    DropdownMenuItem(
                        text = { Text("Any") },
                        onClick = { category = null; categoryExpanded = false },
                    )
                    availableCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name, color = TaskUIHelper.pickColor(cat), fontWeight = FontWeight.Bold) },
                            onClick = { category = cat; categoryExpanded = false },
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Assignee filter (excludes self — backend enforces it too)
            ExposedDropdownMenuBox(
                expanded = assigneeExpanded,
                onExpandedChange = { assigneeExpanded = !assigneeExpanded },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
            ) {
                TextField(
                    value = assignee ?: ANY_OPTION,
                    onValueChange = {},
                    label = { Text("Assignee", fontSize = 11.sp) },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assigneeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                        .pointerHoverIcon(PointerIcon.Hand, true),
                )
                ExposedDropdownMenu(
                    expanded = assigneeExpanded,
                    onDismissRequest = { assigneeExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(ANY_OPTION) },
                        onClick = { assignee = null; assigneeExpanded = false },
                    )
                    members.forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u.name) },
                            onClick = { assignee = u.username; assigneeExpanded = false },
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
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
                        containerColor = TaskUIHelper.getLightGray(),
                        contentColor = Color.Black,
                    ),
                ) { Text("Create Task") }
            }

            Spacer(Modifier.height(8.dp))

            toastMessage?.let {
                ToastMessage(
                    message = it,
                    isError = toastIsError,
                    onDismiss = { toastMessage = null },
                )
            }

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
                                        is ApiResult.Error -> {
                                            toastIsError = true
                                            toastMessage = result.message
                                        }
                                        is ApiResult.NotFound -> {
                                            toastIsError = true
                                            toastMessage = result.message
                                        }
                                    }
                                }
                            },
                            onUpdate = { currentTaskToUpdate = task },
                            onDetails = { currentDetailTask = task },
                            onUnassign = {
                                scope.launch {
                                    val result = taskApi.unassignTask(task)
                                    if (result is ApiResult.Error) {
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
