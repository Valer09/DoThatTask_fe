package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocalStrings
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.CategoryApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper

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
    var categories by remember(groupId) { mutableStateOf<List<TaskCategory>>(emptyList()) }
    var error by remember(groupId) { mutableStateOf<String?>(null) }
    var createCategoryOpen by remember { mutableStateOf(false) }
    // The chip-tap opens an Edit dialog. Holding the category locally so
    // the dialog is composed only when we have one; setting back to null
    // dismisses it.
    var editingCategory by remember(groupId) { mutableStateOf<TaskCategory?>(null) }
    val s = LocalStrings.current

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

    editingCategory?.let { target ->
        CategoryEditDialog(
            groupId = groupId,
            category = target,
            onConfirm = { reload() },
            onClose = { editingCategory = null },
        )
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
                "${s.groupsCategories.replaceFirstChar { it.uppercase() }} (${categories.size})",
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
                    // Whole chip is the tap target — opens the edit dialog
                    // (which is also where the delete trash lives now).
                    Text(
                        text = cat.name,
                        color = TaskUIHelper.contrastingTextColor(TaskUIHelper.parseHexColor(cat.color)),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(TaskUIHelper.parseHexColor(cat.color).copy(alpha = 0.2f))
                            .clickable { editingCategory = cat }
                            .pointerHoverIcon(PointerIcon.Hand, true)
                            .padding(horizontal = 30.dp, vertical = 6.dp),
                    )
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
                Text(s.addCategoryButton)
            }
        }
    }
}
