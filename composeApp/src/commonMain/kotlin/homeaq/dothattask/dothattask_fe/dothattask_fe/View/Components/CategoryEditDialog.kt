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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.CategoryApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper
import kotlinx.coroutines.launch

/**
 * Edit dialog for an existing category. Mirrors [CategoryCreationDialog]'s
 * shape so the UI feels symmetric: header + name/color fields + a primary
 * action button. Two differences:
 *
 *  - A red trash IconButton sits in the header, opposite the title — it
 *    wires through to [CategoryApi.unlink] and works today.
 *  - The 'Save' button is **intentionally a no-op** for now: the backend
 *    doesn't yet expose an update endpoint, so we call [onConfirm] (so
 *    the dialog closes / list reloads) but don't push the changes
 *    anywhere. Replace the body of the click handler with a real
 *    `api.update(...)` once the endpoint lands.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditDialog(
    groupId: Int,
    category: TaskCategory,
    onConfirm: suspend () -> Unit,
    onClose: () -> Unit,
) {
    val api = remember { CategoryApi(client()) }
    var name by remember(category.id) { mutableStateOf(category.name) }
    var color by remember(category.id) { mutableStateOf(category.color.orEmpty()) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val fieldColors = TaskUIHelper.appTextFieldColors()

    Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
        LoadingOverlay(isLoading = loading, Color.Transparent)

        Dialog(onDismissRequest = {}) {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(5.dp),
                    shape = TaskUIHelper.appCardShape(),
                    colors = TaskUIHelper.appCardColors(),
                ) {
                    toastMessage?.let {
                        ToastMessage(
                            message = it,
                            isError = toastIsError,
                            onDismiss = { toastMessage = null; toastIsError = false },
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TaskUIHelper.getPrimary())
                            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "Edit category",
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = {
                                scope.launch {
                                    loading = true
                                    when (val res = api.unlink(groupId, category.id)) {
                                        is ApiResult.Success -> {
                                            onConfirm()
                                            onClose()
                                        }
                                        is ApiResult.Error -> if (!res.routeIfNetwork()) {
                                            toastMessage = res.message
                                            toastIsError = true
                                        }
                                        is ApiResult.NotFound -> {
                                            toastMessage = res.message
                                            toastIsError = true
                                        }
                                        is ApiResult.Unauthorized -> {
                                            toastMessage = "Unauthorized"
                                            AppState.currentScreen = Screen.Login
                                        }
                                        is ApiResult.Forbidden -> {
                                            toastMessage = "Forbidden"
                                            toastIsError = true
                                        }
                                    }
                                    loading = false
                                }
                            },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete category",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(10.dp)) {
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            colors = fieldColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        TextField(
                            value = color,
                            onValueChange = { color = it },
                            label = { Text("Color (#RRGGBB)") },
                            colors = fieldColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        )

                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            OutlinedButton(
                                modifier = Modifier.appButtonSizeSmall()
                                    .pointerHoverIcon(PointerIcon.Hand, true),
                                onClick = { onClose() },
                            ) {
                                Text("Close")
                            }
                            OutlinedButton(
                                onClick = {
                                    // No-op for now: the BE doesn't yet expose
                                    // a category update endpoint. We close the
                                    // dialog and trigger a reload anyway so the
                                    // flow is identical to the real future call.
                                    scope.launch {
                                        onConfirm()
                                        onClose()
                                    }
                                },
                                enabled = !loading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TaskUIHelper.getComplementary(),
                                    contentColor = Color.Black,
                                ),
                                modifier = Modifier.appButtonSizeSmall()
                                    .pointerHoverIcon(PointerIcon.Hand, true),
                            ) { Text("Save") }
                        }
                    }
                }
            }
        }
    }
}
