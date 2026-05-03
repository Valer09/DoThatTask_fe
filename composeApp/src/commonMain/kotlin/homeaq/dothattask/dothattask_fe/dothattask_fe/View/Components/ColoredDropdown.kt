package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.CategoryApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ColoredDropdown(
    items: List<T>,
    selected: T,
    label: String,
    itemLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemColor: ((T) -> Color)? = null,
    dropdownFieldColors: TextFieldColors? = null,
    cardSurfaceColor: Color? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    val resolvedCardSurface = cardSurfaceColor ?: MaterialTheme.colorScheme.surface
    val resolvedFieldColors = dropdownFieldColors ?: TextFieldDefaults.colors(
        focusedLabelColor = MaterialTheme.colorScheme.onSurface,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
    )

    val cardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val cardShape = RoundedCornerShape(12.dp)
    val cardModifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    val defaultColor = MaterialTheme.colorScheme.onSurface
    val resolvedItemColor: (T) -> Color = itemColor ?: { defaultColor }

    Card(modifier = cardModifier, shape = cardShape, colors = cardColors) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Hand, true),
        ) {
            TextField(
                value = itemLabel(selected),
                colors = resolvedFieldColors.copy(
                    focusedTextColor = resolvedItemColor(selected),
                    unfocusedTextColor = resolvedItemColor(selected),
                ),
                onValueChange = {},
                label = { Text(label, fontSize = 10.sp) },
                textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .pointerHoverIcon(PointerIcon.Hand, true),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
            ) {
                items.forEach { item ->
                    val color = resolvedItemColor(item)
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    DropdownMenuItem(
                        text = {
                            Text(itemLabel(item), fontWeight = FontWeight.Bold, color = color, fontSize = 17.sp)
                        },
                        onClick = { onSelect(item); expanded = false },
                        interactionSource = interactionSource,
                        colors = MenuItemColors(
                            textColor = color,
                            leadingIconColor = Color.Unspecified,
                            trailingIconColor = Color.Unspecified,
                            disabledTextColor = color.copy(alpha = 0.4f),
                            disabledLeadingIconColor = Color.Unspecified,
                            disabledTrailingIconColor = Color.Unspecified,
                        ),
                        modifier = Modifier
                            .background(if (isPressed) color.copy(alpha = 0.25f) else resolvedCardSurface)
                            .pointerHoverIcon(PointerIcon.Hand, true),
                    )
                    if (item != items.last()) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            thickness = 1.dp,
                        )
                    }
                }
            }
        }
    }
    }