package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskStatus
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocalStrings
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper


@Composable
fun TaskCard(
    task: Task,
    onUpdate: (Task) -> Unit,
    onDetails: (Task) -> Unit,
    hideDelete: Boolean = false,
    hideUpdate: Boolean = false,
    onUnassign: (Task) -> Unit,
) {

    val s = LocalStrings.current

    Card(
        // The whole card is the tap target for the detail dialog now.
        // Removed the dedicated 'Details' OutlinedButton from the action
        // row below — a separate button felt redundant when every other
        // tabular card in the app (groups, invites, completed) opens its
        // detail by tap on the row itself.
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { onDetails(task) }
            .pointerHoverIcon(PointerIcon.Hand, true),
        shape = RoundedCornerShape(CornerSize(8.dp)),
        colors = CardDefaults.cardColors(containerColor = TaskUIHelper.pickColor(task.category))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    task.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                )
                if (task.groupName.isNotBlank()) {
                    GroupBadge(task.groupName, task.groupColor)
                }
            }
            Spacer(Modifier.height(7.dp))
            Text(
                task.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(10.dp))
            Row {
                if (task.status == TaskStatus.ACTIVE) {
                    Spacer(Modifier.width(10.dp))
                    OutlinedButton(
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, hideDelete),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black,
                            containerColor = TaskUIHelper.getLightGray(),
                        ),
                        onClick = { onUnassign(task) },
                    ) {
                        Text(s.unassignButton.replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }
    }
}
