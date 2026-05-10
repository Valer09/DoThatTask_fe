package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.zIndex
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.User
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupSummary
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocalStrings
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
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.appButtonSizeSmall
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
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
    var creator by remember { mutableStateOf<String?>(null) }
    var category by remember { mutableStateOf<TaskCategory?>(null) }
    var assignee by remember { mutableStateOf<User?>(null) }
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var membersLoading by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }
    var currentTaskToUpdate by remember { mutableStateOf<Task?>(null) }
    var currentDetailTask by remember { mutableStateOf<Task?>(null) }
    var taskCreationOpen by remember { mutableStateOf(false) }
    val s = LocalStrings.current

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
            val creatorParam = when (creator) {
                CREATOR_ME_OPTION -> AuthState.username
                null -> null
                else -> creator
            }
            when (val res = taskApi.searchTasks(gid, category, assignee?.username)) {
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
                toastMessage = "Task '${created.name}' ${s.created}"
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
                toastMessage = "Task '${updated.name}' ${s.updated}"
                scope.launch { runSearch() }
            },
            onDismiss = { currentTaskToUpdate = null },
            onDelete = {
                scope.launch {
                    when (val result = taskApi.removeTask(it)) {
                        is ApiResult.Success -> {
                            toastIsError = false
                            toastMessage = result.message
                            currentTaskToUpdate = null
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
                            toastMessage = s.unauthorizedError
                            AppState.currentScreen = Screen.Login
                        }
                        is ApiResult.Forbidden -> {
                            toastMessage = s.forbiddenError
                        }
                    }
                }
            },
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
        val groups = AuthState.groups
        val anyCategory = TaskCategory(id = -1, name = ANY_CATEGORY)
        val categoryOptions = buildList {
            add(anyCategory)
            addAll(availableCategories)
        }
        val color =MaterialTheme.colorScheme.onSurface
        val anyUser= User(s.anyUser.replaceFirstChar { it.uppercase() }, "")
        val userOptions = buildList {
            add(anyUser)
            addAll(members)
        }

        val visible = tasks.filter { !it.ownership_username.equals(AuthState.username, ignoreCase = true) }
        val listState = rememberLazyListState()

        val showScrollToTop by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0 ||
                        listState.firstVisibleItemScrollOffset > 200
            }}


        toastMessage?.let {
            Row(modifier = Modifier.fillMaxWidth().zIndex(10f), verticalAlignment = Alignment.Top) {
                ToastMessage(message = it, isError = toastIsError, onDismiss = { toastMessage = null })
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        )
        {
            if (groups.isEmpty()) {
                item {
                    Text(
                        s.groupPageEmptyState.replaceFirstChar { it.uppercase() },
                        color = Color.Gray,
                    )
                }
                return@LazyColumn
            }

            item{
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { taskCreationOpen = true },
                        modifier = Modifier
                            .appButtonSizeSmall()
                            .pointerHoverIcon(PointerIcon.Hand, true),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TaskUIHelper.getComplementary(),
                            contentColor = Color.Black,
                        ),
                    ) {
                        Text(s.createTaskButton.replaceFirstChar { it.uppercase() })
                    }
                }
            }
            item {
                ColoredDropdown(
                    items = groups,
                    selected = selectedGroup ?: groups.first(),
                    label = s.group.replaceFirstChar { it.uppercase() },
                    itemLabel = { it.name },
                    onSelect = { selectedGroup = it },
                    itemColor = { TaskUIHelper.parseHexColor(it.color) }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ColoredDropdown(
                        modifier = Modifier.weight(1f),
                        items = categoryOptions,
                        selected = category ?: anyCategory,
                        label = s.taskCategory.replaceFirstChar { it.uppercase() },
                        itemLabel = { it.name },
                        itemColor = { if (it.name == ANY_CATEGORY) color else TaskUIHelper.pickColor(it) },
                        onSelect = { selected -> category = if (selected.id < 0) null else selected }
                    )
                    ColoredDropdown(
                        modifier = Modifier.weight(1f),
                        items = userOptions,
                        selected = assignee ?: anyUser,
                        label = s.taskAssignee.replaceFirstChar { it.uppercase() },
                        itemLabel = { it.name },
                        itemColor = { color },
                        onSelect = { assignee = if (it.username == "any") null else it }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        alignment = Alignment.CenterHorizontally
                    )
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TaskUIHelper.getSecondary(),
                            contentColor = TaskUIHelper.getAlternativeText()
                        ),
                        onClick = { scope.launch { runSearch() } },
                        modifier = Modifier
                            .appButtonSizeSmall()
                            .pointerHoverIcon(PointerIcon.Hand, true)
                    ) {
                        Text(s.searchButtonParameter.replaceFirstChar { it.uppercase() })
                    }
                }
            }

            if (visible.isEmpty() && !loading) {
                item {
                    Text(s.taskSearchEmptyState.replaceFirstChar { it.uppercase() }, color = Color.Gray)
                }
            } else {
                items(
                    items = visible,
                    key = { it.name }
                ) { task ->
                    TaskCard(
                        task,
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
                                    toastMessage = s.taskDisconnected
                                }
                                runSearch()
                            }
                        },
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
            }
        }

        AnimatedVisibility(
            visible = showScrollToTop,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = TaskUIHelper.getSecondary(),
                contentColor = TaskUIHelper.getAlternativeText()
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Scroll to top"
                )
            }
        }


        LoadingOverlay(isLoading = loading || membersLoading)
    }
}
