package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper


@Composable
fun UserListDropdown(
    task: Task,
    onDelete: (Task) -> Unit,
    onUpdate: (Task) -> Unit,
    onDetails: (Task) -> Unit,
) {

    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        shape = RoundedCornerShape(CornerSize(8.dp)),
        colors = CardDefaults.cardColors(containerColor = TaskUIHelper.pickColor(task.category))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                "${task.name}: ${task.description}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(Modifier.height(16.dp))
            Row {
                OutlinedButton(
                    modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black, containerColor = TaskUIHelper.getRed()),
                    onClick = { onDelete(task) })
                {
                    Text("Delete")
                }
                Spacer(Modifier.width(16.dp))
                OutlinedButton(
                    modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black, containerColor = TaskUIHelper.getGreen()),
                    onClick = { onUpdate(task) }
                )
                {
                    Text("Update")
                }

                Spacer(Modifier.width(16.dp))
                OutlinedButton(
                    modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black, containerColor = TaskUIHelper.getGray()),
                    onClick = { onDetails(task) }
                )
                {
                    Text("Details")
                }
            }
        }
    }
}