package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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

/**
 * Small composable that renders a Card with a floating label overlaid on the
 * top-left edge, matching the style of Material OutlinedTextField labels.
 */
@Composable
fun LabeledCard(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val cardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val cardShape = RoundedCornerShape(12.dp)

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),     // leave space for the label to sit on the border
            shape = cardShape,
            colors = cardColors,
        ) {
            Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)) {
                content()
            }
        }

        // Floating label – sits on top of the card border
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.TopStart)
                // Tiny background pill so the label masks the card border cleanly
                .padding(horizontal = 4.dp),
        )
    }
}

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

    LaunchedEffect(selectedGroupId) {
        try {
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
                else -> {
                    if (result is ApiResult.Error && result.routeIfNetwork()) {
                        toastIsError = true; toastMessage = result.message
                    } else if (result is ApiResult.Error && result.routeIfNetwork()) {
                        AppState.currentScreen = Screen.Error
                    }
                }
            }
        } catch (e: Exception) {
            AppState.errorMessage = "Unexpected Error"
            AppState.currentScreen = Screen.Error
        } finally { loading = false }
    }

    LaunchedEffect(selectedGroupId) { loadAssignedTask() }

    Box {
        toastMessage?.let {
            Row(modifier = Modifier.fillMaxWidth().zIndex(10f), verticalAlignment = Alignment.Top) {
                ToastMessage(message = it, isError = toastIsError, onDismiss = { toastMessage = null })
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
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

                // ── Task title (fixed, outside any card) ───────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = assignedTask?.name ?: "",
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 23.sp,
                    )
                }

                // ── Group + Category side by side ──────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LabeledCard(
                        label = "Group",
                        modifier = Modifier.weight(1f).height(70.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            assignedTask?.let { t ->
                                if (t.groupName.isNotBlank()) {
                                    GroupBadge(t.groupName, t.groupColor)
                                } else {
                                    Text("—", color = onSurfaceColor)
                                }
                            }
                        }
                    }

                    LabeledCard(
                        label = "Category",
                        modifier = Modifier.weight(1f).height(70.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            assignedTask?.category?.let { cat ->
                                Text(
                                    text = cat.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontSize = 16.sp,
                                    color = TaskUIHelper.pickColor(cat),
                                )
                            } ?: Text("—", color = onSurfaceColor)
                        }
                    }
                }

                // ── Description card (scrollable) ──────────────────────────
                LabeledCard(
                    label = "Description",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .weight(1f),          // takes remaining vertical space
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = assignedTask?.description ?: "No description available.",
                            color = onSurfaceColor,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 16.sp,
                        )
                    }
                }

            } else {
                // ── No task assigned ──────────────────────────────────────
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "No task assigned. Pick a task",
                            style = MaterialTheme.typography.bodyLarge,
                            color = onSurfaceColor.copy(alpha = 0.6f),
                            fontSize = 25.sp,
                        )
                    }
                }
            }

            // ── Bottom buttons ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand, true)
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    onClick = { scope.launch { AppState.currentScreen = Screen.CompletedTask } },
                ) {
                    Text("Completed tasks", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
                }

                if (assignedTask != null) {
                    Button(
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand, true)
                            .weight(1f)
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        onClick = { scope.launch { complete() } },
                        colors = ButtonDefaults.buttonColors(containerColor = TaskUIHelper.getComplementary()),
                    ) {
                        Text("Complete task!", color = Color.Black, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                } else {
                    Button(
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand, true)
                            .weight(1f)
                            .padding(horizontal = 5.dp, vertical = 8.dp),
                        onClick = { scope.launch { pickTask(category) } },
                        colors = ButtonDefaults.buttonColors(containerColor = TaskUIHelper.getComplementary()),
                    ) {
                        Text("Pick a new task!", color = Color.White, fontSize = 17.sp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }

        LoadingOverlay(isLoading = loading)
    }
}