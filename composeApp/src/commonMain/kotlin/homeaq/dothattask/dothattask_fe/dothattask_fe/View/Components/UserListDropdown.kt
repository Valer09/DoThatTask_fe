package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.User
import org.jetbrains.compose.ui.tooling.preview.Preview


/**
 * A reusable user-picker dropdown. Callers supply the list of users (so the
 * same component can be driven by group-aware data — e.g. members of a
 * specific group during create/update). Pre-selects [selectedUsername] when
 * present.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun UserListDropdown(
    label: String,
    users: List<User>,
    isLoading: Boolean,
    selectedUsername: String?,
    onUserSelected: (User) -> Unit,
    onLoad: (User) -> Unit = {},
    height: Dp = 60.dp,
    labelSize: TextUnit = 11.sp,
    fontSizea: TextUnit = 13.sp,
)
{
    var selectedUser by remember(selectedUsername, users) {
        mutableStateOf(
            selectedUsername?.let { name -> users.firstOrNull { it.username == name } }
                ?: users.firstOrNull(),
        )
    }
    var userDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(users, selectedUsername) {
        selectedUser?.let { onLoad(it) }
    }

    Row(modifier = Modifier.fillMaxWidth())
    {
        ExposedDropdownMenuBox(
            expanded = userDropdownExpanded,
            onExpandedChange = { userDropdownExpanded = !userDropdownExpanded },
            modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true)) {
            TextField(
                value = if (isLoading) "Loading. . ." else selectedUser?.name ?: "",
                onValueChange = {},
                label = {Text(label, fontSize = labelSize) },
                textStyle = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSizea
                ),
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .pointerHoverIcon(PointerIcon.Hand, true)
                    .fillMaxWidth().height(height)
            )
            val pointerHoverIcon = Modifier.pointerHoverIcon(PointerIcon.Hand, true)
            ExposedDropdownMenu(
                expanded = userDropdownExpanded,
                onDismissRequest = { userDropdownExpanded = false },
                modifier = pointerHoverIcon.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            ) {
                users.forEach { user ->
                    DropdownMenuItem(
                        text = { Text(user.name, fontSize = fontSizea) },
                        onClick = {
                            selectedUser = user
                            userDropdownExpanded = false
                            onUserSelected(user)
                        },
                        modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true).height(height)
                    )
                }
            }
        }
    }
}
