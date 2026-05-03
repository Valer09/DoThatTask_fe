package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.CategoryApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.ColoredDropdown
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.GroupBadge
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.ToastMessage


import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun MainPage() {
    val taskApi = remember { TaskApi(client()) }
    val categoryApi = remember { CategoryApi(client()) }
    var assignedTask by remember { mutableStateOf<Task?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }
    var selectedGroupId by remember { mutableStateOf(AuthState.activeGroupId) }
    var availableCategories by remember { mutableStateOf(TaskCategory.Defaults) }
    var category by remember { mutableStateOf(TaskCategory.Social) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val cardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val cardShape = RoundedCornerShape(12.dp)
    val cardModifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)

    LaunchedEffect(selectedGroupId) {
        AuthState.activeGroupId = selectedGroupId
        val gid = selectedGroupId ?: return@LaunchedEffect
        when (val result = categoryApi.list(gid)) {
            is ApiResult.Success -> {
                if (result.data.isNotEmpty()) {
                    availableCategories = result.data
                    if (availableCategories.none { it.id == category.id })
                        category = availableCategories.first()
                }
            }
            else -> {}
        }
    }

    suspend fun loadAssignedTask() {
        val gid = selectedGroupId ?: run { assignedTask = null; return }
        loading = true
        try {
            val result = taskApi.getAssignedTask(gid)
            if (result is ApiResult.Success) assignedTask = result.data
            else if (result is ApiResult.NotFound) assignedTask = null
            else if (result is ApiResult.Error && !result.routeIfNetwork()) {
                toastIsError = true; toastMessage = result.message
            }
        } catch (e: Exception) {
            toastIsError = true; toastMessage = "Failed to load users: ${e.message}"
        } finally { loading = false }
    }

    suspend fun pickTask(category: TaskCategory) {
        val gid = selectedGroupId ?: run { toastIsError = true; toastMessage = "Pick a group first"; return }
        loading = true
        try {
            val result = taskApi.pickTask(gid, category)
            if (result is ApiResult.Success) loadAssignedTask()
            else if (result is ApiResult.NotFound) {
                toastIsError = false
                toastMessage = "No assignable task in this category for this user."
                loadAssignedTask()
            } else if (result is ApiResult.Error && !result.routeIfNetwork()) {
                toastIsError = true; toastMessage = result.message
            }
        } catch (e: Exception) {
            toastIsError = true; toastMessage = "Failed to pick a task: ${e.message}"
        } finally { loading = false }
    }

    suspend fun complete() {
        loading = true
        try {
            val result = taskApi.completeTask(assignedTask)
            if (result is ApiResult.Success) loadAssignedTask()
            else if (result is ApiResult.Error && !result.routeIfNetwork()) {
                toastIsError = true; toastMessage = result.message
            }
        } catch (e: Exception) {
            toastIsError = true; toastMessage = "Failed to complete the task: ${e.message}"
        } finally { loading = false }
    }

    LaunchedEffect(selectedGroupId) { loadAssignedTask() }

    Box {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)) {

            // Toast
            toastMessage?.let {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = (-20).dp), verticalAlignment = Alignment.Top) {
                    ToastMessage(message = it, isError = toastIsError, onDismiss = { toastMessage = null })
                }
            }

            val groups = AuthState.groups
            val selectedGroup = groups.firstOrNull { it.id == selectedGroupId }

            if (groups.isNotEmpty()) {
                ColoredDropdown(
                    items = groups,
                    selected = selectedGroup ?: groups.first(),
                    label = "Active Group",
                    itemLabel = { it.name },
                    onSelect = { selectedGroupId = it.id },
                    itemColor = { TaskUIHelper.parseHexColor(it.color) },
                )

            }

            if (assignedTask == null) {
                Spacer(modifier = Modifier.height(4.dp))
                ColoredDropdown(
                    items = availableCategories,
                    selected = category,
                    label = "Category",
                    itemLabel = { it.name },
                    itemColor = { TaskUIHelper.pickColor(it) },
                    onSelect = { category = it },
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (assignedTask != null) {
                Card(
                    modifier = cardModifier.weight(0.70f),
                    shape = cardShape,
                    colors = cardColors,
                ) {
                    Column(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.weight(0.1f).fillMaxWidth().padding(top = 15.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "${assignedTask?.name}",
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor,
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 23.sp,
                            )
                        }

                        Row(
                            modifier = Modifier.weight(0.9f).padding(vertical = 15.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,

                        ) {
                            Column {
                                Row {
                                    Column(modifier = Modifier.weight(0.3f).padding(vertical = 8.dp)) {
                                        Text("Group:", fontWeight = FontWeight.Bold, color = onSurfaceColor, style = MaterialTheme.typography.bodyLarge, fontSize = 17.sp)
                                    }
                                    Column(modifier = Modifier.weight(0.7f).padding(vertical = 8.dp).padding(horizontal = 4.dp)) {
                                        assignedTask?.let { t ->
                                            if (t.groupName.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(10.dp))
                                                GroupBadge(t.groupName, t.groupColor)
                                            }
                                        }
                                    }
                                }

                                Row {
                                    Column(modifier = Modifier.weight(0.3f).padding(vertical = 8.dp)) {
                                        Text("Category:", fontWeight = FontWeight.Bold, color = onSurfaceColor, style = MaterialTheme.typography.bodyLarge, fontSize = 17.sp)
                                    }
                                    Column(modifier = Modifier.weight(0.7f).padding(vertical = 8.dp).padding(horizontal = 4.dp)) {
                                        assignedTask?.category?.let {
                                            Text(text = it.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, fontSize = 17.sp, color = TaskUIHelper.pickColor(it))
                                        }
                                    }
                                }
                                Row {
                                    Column(modifier = Modifier.weight(0.3f).padding(vertical = 8.dp)) {
                                        Text("Description:", fontWeight = FontWeight.Bold, color = onSurfaceColor, style = MaterialTheme.typography.bodyLarge, fontSize = 17.sp)
                                    }
                                    Column(modifier = Modifier.weight(0.7f).padding(vertical = 8.dp).padding(horizontal = 4.dp)) {
                                        Text(text = assignedTask?.description ?: "N/A", color = onSurfaceColor, style = MaterialTheme.typography.bodyLarge, fontSize = 17.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {

                Card(
                    modifier = cardModifier.weight(0.35f),
                    shape = cardShape,
                    colors = cardColors,
                ) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No task assigned. Pick a task",
                                style = MaterialTheme.typography.bodyLarge,
                                color = onSurfaceColor.copy(alpha = 0.6f),
                                fontSize = 25.sp
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .weight(0.35f),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand, true)
                        .weight(1f)
                        .padding(horizontal = 5.dp, vertical = 8.dp),
                    onClick = { scope.launch { AppState.currentScreen = Screen.CompletedTask } },
                ) {
                    Text("Completed tasks", color = Color.White, fontSize = 17.sp, modifier = Modifier.padding(vertical = 8.dp))
                }
                if (assignedTask != null) {
                    Button(
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand, true)
                            .weight(1f)
                            .padding(horizontal = 5.dp, vertical = 8.dp),
                        onClick = { scope.launch { complete() } },
                        colors = ButtonDefaults.buttonColors(containerColor = TaskUIHelper.getComplementary())
                    ) {
                        Text("Complete task!", color = Color.Black, fontSize = 17.sp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                } else {
                    Button(
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand, true)
                            .weight(1f)
                            .padding(horizontal = 5.dp, vertical = 8.dp),
                        onClick = { scope.launch { pickTask(category) } },
                        colors = ButtonDefaults.buttonColors(containerColor = TaskUIHelper.getComplementary())
                    ) {
                        Text("Pick a new task!", color = Color.White, fontSize = 17.sp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }

        LoadingOverlay(isLoading = loading)
    }
}