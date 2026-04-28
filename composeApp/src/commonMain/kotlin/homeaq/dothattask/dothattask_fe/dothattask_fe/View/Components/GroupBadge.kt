package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper

/**
 * Small colored pill that identifies which group a task / invite came from.
 * The background is the group's color; the foreground is auto-picked for
 * contrast.
 */
@Composable
fun GroupBadge(
    groupName: String,
    groupColor: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp,
) {
    if (groupName.isBlank()) return
    val bg = TaskUIHelper.parseHexColor(groupColor)
    val fg = TaskUIHelper.contrastingTextColor(bg)
    Text(
        text = groupName,
        color = fg,
        fontWeight = FontWeight.SemiBold,
        fontSize = fontSize,
        modifier = modifier
            .background(color = bg, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
