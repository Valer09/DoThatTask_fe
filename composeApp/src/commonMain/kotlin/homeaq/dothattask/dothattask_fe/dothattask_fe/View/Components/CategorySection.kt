package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.CategoryApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper
import kotlinx.coroutines.launch

/**
 * Categories panel rendered inside a group card. Lists the categories
 * linked to [groupId], lets any group member add new ones, and exposes a
 * one-click unlink for each (the backend rejects unlinking a category that
 * tasks still reference).
 *
 * Adding behavior:
 *   - Type a name + optional hex color, hit "Add".
 *   - The server normalizes the name (capitalize first letter, lowercase
 *     rest) and dedups case-insensitively against the global table —
 *     same name in any case → reuse, otherwise insert + link.
 */
@Composable
fun GroupCategoriesSection(groupId: Int) {
    val api = remember { CategoryApi(client()) }
    val scope = rememberCoroutineScope()

    var categories by remember(groupId) { mutableStateOf<List<TaskCategory>>(emptyList()) }
    var newName by remember(groupId) { mutableStateOf("") }
    var newColor by remember(groupId) { mutableStateOf("") }
    var error by remember(groupId) { mutableStateOf<String?>(null) }
    var loading by remember(groupId) { mutableStateOf(false) }

    suspend fun reload() {
        when (val res = api.list(groupId)) {
            is ApiResult.Success -> categories = res.data
            is ApiResult.Error -> error = res.message
            else -> {}
        }
    }
    LaunchedEffect(groupId) { reload() }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("Categories (${categories.size})", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))

        categories.forEach { cat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = cat.name,
                    color = TaskUIHelper.contrastingTextColor(TaskUIHelper.parseHexColor(cat.color)),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .background(TaskUIHelper.parseHexColor(cat.color), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = {
                        scope.launch {
                            loading = true
                            when (val res = api.unlink(groupId, cat.id)) {
                                is ApiResult.Success -> reload()
                                is ApiResult.Error -> error = res.message
                                is ApiResult.NotFound -> error = res.message
                                is ApiResult.Unauthorized -> {
                                    error = "Unauthorized"
                                    AppState.currentScreen = Screen.Login
                                }
                            }
                            loading = false
                        }
                    },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                ) { Text("✕", color = Color.Red, fontWeight = FontWeight.Bold) }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New category", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1.5f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TaskUIHelper.getLightGray(),
                    unfocusedContainerColor = TaskUIHelper.getGray(),
                ),
            )
            Spacer(Modifier.width(6.dp))
            OutlinedTextField(
                value = newColor,
                onValueChange = { newColor = it },
                label = { Text("Color (#RRGGBB)", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TaskUIHelper.getLightGray(),
                    unfocusedContainerColor = TaskUIHelper.getGray(),
                ),
            )
            Spacer(Modifier.width(6.dp))
            Button(
                onClick = {
                    val name = newName.trim()
                    if (name.isEmpty()) {
                        error = "Category name cannot be empty"
                        return@Button
                    }
                    scope.launch {
                        loading = true
                        when (val res = api.create(groupId, name, newColor.takeIf { it.isNotBlank() })) {
                            is ApiResult.Success -> {
                                newName = ""
                                newColor = ""
                                error = null
                                reload()
                            }
                            is ApiResult.Error -> error = res.message
                            is ApiResult.NotFound -> error = res.message
                            is ApiResult.Unauthorized -> {
                                error = "Unauthorized"
                                AppState.currentScreen = Screen.Login
                            }
                        }
                        loading = false
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskUIHelper.getMarinerBlue(),
                    contentColor = Color.White,
                ),
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true).height(48.dp),
            ) { Text("Add") }
        }

        error?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, color = Color.Red, fontSize = 12.sp)
        }
    }
}
