package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.ui.graphics.Color
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory

class TaskUIHelper
{
    companion object
    {
        /**
         * Categories carry their own color now (set per-group on the
         * backend). Defer to that, falling back to the mariner blue used
         * elsewhere in the UI when the color is missing or malformed.
         */
        fun pickColor(category: TaskCategory): Color = parseHexColor(category.color)

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

        /**
         * Parses a hex color string (e.g. "#7E57C2" or "#AARRGGBB") into a
         * Compose [Color]. Falls back to mariner blue when the string is
         * missing or malformed so a stale group color never breaks the UI.
         */
        fun parseHexColor(hex: String?): Color {
            if (hex.isNullOrBlank()) return getMarinerBlue()
            val raw = hex.removePrefix("#")
            val padded = when (raw.length) {
                6 -> "FF$raw"
                8 -> raw
                else -> return getMarinerBlue()
            }
            val value = padded.toLongOrNull(16) ?: return getMarinerBlue()
            return Color(value)
        }

        /**
         * Picks a foreground (text) color that contrasts with [background],
         * using the standard WCAG luminance formula.
         */
        fun contrastingTextColor(background: Color): Color {
            val r = background.red
            val g = background.green
            val b = background.blue
            val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
            return if (luminance > 0.55) Color.Black else Color.White
        }
    }
}
