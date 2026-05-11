package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocalStrings
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailDialog(
    task: Task,
    onConfirm: (Task) -> Unit,
    onUpdate: (Task) -> Unit,
    onDismiss: () -> Unit,
    hideUpdate: Boolean,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val s = LocalStrings.current

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            shape = TaskUIHelper.appCardShape(),
            colors = TaskUIHelper.appCardColors(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(TaskUIHelper.getPrimary()).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            )
            {
                Text("${task.name}: details", fontSize = 20.sp, color = Color.White)
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Column(horizontalAlignment = Alignment.Start) {
                        if (task.groupName.isNotBlank()) {
                            GroupBadge(task.groupName, task.groupColor)
                            Spacer(Modifier.height(5.dp))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding( horizontal = 5.dp)){
                        if (!hideUpdate) {
                            IconButton(
                                onClick = { onUpdate(task) },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand).size(25.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Edit",
                                    tint = TaskUIHelper.getComplementary(),
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("Assigned to: @${task.ownership_username}", color = onSurface)
                Spacer(Modifier.height(2.dp))
                Text("Category: ${task.category.name}", color = onSurface)
                Spacer(Modifier.height(25.dp))
                OutlinedTextField(
                    readOnly = hideUpdate,
                    value = task.description,
                    onValueChange = {},
                    label = { Text(s.taskDescription.replaceFirstChar { it.lowercase() }) },
                    colors = TaskUIHelper.appTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                )

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                )
                {
                    OutlinedButton(
                        modifier = Modifier.appButtonSizeSmall().pointerHoverIcon(PointerIcon.Hand, hideUpdate),
                        onClick = { onDismiss() },
                    )
                    {
                        Text("Close")
                    }
                }
            }
        }
    }
}
