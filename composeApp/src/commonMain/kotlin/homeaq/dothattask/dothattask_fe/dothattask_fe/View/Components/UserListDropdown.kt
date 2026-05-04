package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.User


/**
 * User-picker dropdown that defers all rendering to [ColoredDropdown] so the
 * styling stays in lockstep with every other dropdown in the app. While the
 * caller is still loading members (or has none) we surface a non-selectable
 * placeholder row so the field never collapses to an empty state.
 */
@Composable
fun UserListDropdown(
    label: String,
    users: List<User>,
    isLoading: Boolean,
    selectedUsername: String?,
    onUserSelected: (User) -> Unit,
    onLoad: (User) -> Unit = {},
) {
    val placeholderText = if (isLoading) "Loading..." else "No members"
    val placeholder = User(username = "", name = placeholderText)
    val items = users.ifEmpty { listOf(placeholder) }

    val resolved = users.firstOrNull { it.username == selectedUsername }
        ?: users.firstOrNull()
        ?: placeholder

    LaunchedEffect(users, selectedUsername) {
        (users.firstOrNull { it.username == selectedUsername } ?: users.firstOrNull())
            ?.let(onLoad)
    }

    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val activeColor = MaterialTheme.colorScheme.onSurface

    ColoredDropdown(
        items = items,
        selected = resolved,
        label = label,
        itemLabel = { it.name },
        itemColor = { if (it.username.isBlank()) placeholderColor else activeColor },
        onSelect = { user ->
            // The placeholder row is non-actionable: it only exists so the
            // field has something to render while data is loading.
            if (user.username.isNotBlank()) onUserSelected(user)
        },
        modifier = Modifier,
    )
}
