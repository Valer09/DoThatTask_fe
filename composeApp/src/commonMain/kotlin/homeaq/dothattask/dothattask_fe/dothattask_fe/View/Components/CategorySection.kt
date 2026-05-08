package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var loading by remember(groupId) { mutableStateOf(false) }
    var categories by remember(groupId) { mutableStateOf<List<TaskCategory>>(emptyList()) }
    var error by remember(groupId) { mutableStateOf<String?>(null) }
    var createCategoryOpen by remember { mutableStateOf<Boolean>(false) }

    suspend fun reload() {
        when (val res = api.list(groupId)) {
            is ApiResult.Success -> categories = res.data
            is ApiResult.Error -> if (!res.routeIfNetwork()) error = res.message
            else -> {}
        }
    }
    LaunchedEffect(groupId) { reload() }


    if (createCategoryOpen) {
        CategoryCreationDialog(
            groupId,
            onConfirm = { reload() },
            onClose = { createCategoryOpen = false })
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp)
        ) {
            Text(
                "Categories (${categories.size})",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )

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
                        fontSize = 15.sp,
                        modifier = Modifier
                            .background(TaskUIHelper.parseHexColor(cat.color).copy(alpha = 0.2f), RoundedCornerShape(9.dp))
                            .padding(horizontal = 30.dp, vertical = 3.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            scope.launch {
                                loading = true
                                when (val res = api.unlink(groupId, cat.id)) {
                                    is ApiResult.Success -> reload()
                                    is ApiResult.Error -> if (!res.routeIfNetwork()) error = res.message
                                    is ApiResult.NotFound -> error = res.message
                                    is ApiResult.Unauthorized -> {
                                        error = "Unauthorized"
                                        AppState.currentScreen = Screen.Login
                                    }
                                    is ApiResult.Forbidden -> {
                                        error = "Forbidden"
                                    }
                                }
                                loading = false
                            }
                        },
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                    ) {
                        Text("✕", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            error?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, color = Color.Red, fontSize = 12.sp)
            }
        }

        Column(
            modifier = Modifier
                .padding(top = 4.dp),
            horizontalAlignment = Alignment.End
        ) {
            Button(
                onClick = { createCategoryOpen = true},
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskUIHelper.getComplementary(),
                    contentColor = Color.Black,
                ),
                modifier = Modifier
                    .pointerHoverIcon(PointerIcon.Hand, true).appButtonSizeSmall(),
            ) {
                Text("+ Add category")
            }
        }
    }
}
