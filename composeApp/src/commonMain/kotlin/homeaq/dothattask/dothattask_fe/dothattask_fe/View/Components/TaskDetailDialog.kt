package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailDialog(
    task: Task,
    onConfirm: (Task) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = TextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        focusedContainerColor = TaskUIHelper.getGray(),
        unfocusedContainerColor = TaskUIHelper.getGray(),
    )

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            shape = RoundedCornerShape(CornerSize(4.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(TaskUIHelper.getPrimary()).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            )
            {
                Text("${task.name}: details", fontSize = 20.sp, color = Color.White)
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Spacer(Modifier.height(15.dp))
                if (task.groupName.isNotBlank()) {
                    GroupBadge(task.groupName, task.groupColor)
                    Spacer(Modifier.height(10.dp))
                }
                Text("Assigned to: @${task.ownership_username}")
                Spacer(Modifier.height(10.dp))
                Text("Category: ${task.category.name}")
                Spacer(Modifier.height(10.dp))
                TextField(
                    readOnly = true,
                    value = task.description,
                    onValueChange = {},
                    label = { Text("Description") },
                    colors = colors,
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
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black,
                            containerColor = TaskUIHelper.getGray(),
                        ),
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
