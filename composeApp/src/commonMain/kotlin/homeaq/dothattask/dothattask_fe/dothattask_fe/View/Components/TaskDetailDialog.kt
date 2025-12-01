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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskStatus
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailDialog(
    task: Task,
    onConfirm: (Task) -> Unit,
    onDismiss: () -> Unit)
{
    var name by remember { mutableStateOf(task.name) }
    var description by remember { mutableStateOf(task.description) }
    var category by remember { mutableStateOf(task.category) }
    var taskStatus by remember { mutableStateOf(task.status) }
    val userList = listOf("alice", "bob", "carlo")
    var assignedUser by remember { mutableStateOf(userList.first()) }
    var userExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }


    val colors = TextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        focusedContainerColor = TaskUIHelper.Companion.getGray(),
        unfocusedContainerColor = TaskUIHelper.Companion.getGray(),
    )

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            shape = RoundedCornerShape(CornerSize(4.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(TaskUIHelper.Companion.getMarinerBlue()).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            )
            {
                Text("${task.name}: details", fontSize = 20.sp)
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Spacer(Modifier.height(15.dp))
                TextField(
                    readOnly = true,
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                )

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween)
                {

                    OutlinedButton(
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Default, true),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black, containerColor = TaskUIHelper.Companion.getGray()),
                        onClick = {onDismiss()}
                    )
                    {
                        Text("Close")
                    }

                }
            }
        }
    }
}