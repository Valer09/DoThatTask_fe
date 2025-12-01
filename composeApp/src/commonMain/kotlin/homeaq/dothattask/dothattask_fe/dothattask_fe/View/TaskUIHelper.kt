package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.ui.graphics.Color
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory

class TaskUIHelper
{
    companion object
    {
        fun pickColor(category: TaskCategory) = when (category) {
            TaskCategory.Social -> Color(0xFF2596BE)
            TaskCategory.Career -> Color(0xFFE0A940)
            TaskCategory.Health -> Color(0xFFecb8c3)
        }

        fun getGreen(): Color {
            return Color(0xFF7ec97d)
        }
        fun getGray(): Color {
            return Color(0xffcfcfcf)
        }

        fun getLightGray(): Color {
            return Color(0xffe0e0e0)
        }

        fun getMarinerBlue(): Color {
            return Color(0xff246cd1)
        }

        fun getRed() : Color
        {
            return Color(0xFFe06464)
        }

    }
}