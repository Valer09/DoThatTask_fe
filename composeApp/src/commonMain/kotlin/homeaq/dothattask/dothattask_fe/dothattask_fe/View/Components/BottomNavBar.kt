package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.NotificationCenter
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.bottomNavDestinations
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocalStrings
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.Strings
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.topLevel

/**
 * Floating Revolut-style bottom navigation bar.
 *
 * Renders the 5 top-level destinations declared by [bottomNavDestinations]
 * as a pill-shaped bar inset from the screen edges. Each item shows an
 * icon + label, with the active destination highlighted by a soft pill.
 * The Invites tab gets a count badge driven by [NotificationCenter].
 */
@Composable
fun BottomNavBar() {
    val selectedTopLevel = AppState.currentScreen.topLevel()
    val s = LocalStrings.current

    Surface(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier
                .height(72.dp)
                .padding(horizontal = 4.dp),
        ) {
            bottomNavDestinations.forEach { screen ->
                val (icon, label) = screen.tabPresentation(s)
                NavigationBarItem(
                    selected = selectedTopLevel == screen,
                    onClick = { AppState.changePage(screen.resolveTarget()) },
                    icon = {
                        if (screen == Screen.IncomingInvites) {
                            BadgedBox(
                                badge = {
                                    val count = NotificationCenter.pendingInvitesCount
                                    if (count > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = Color.White,
                                        ) {
                                            Text(
                                                text = if (count > 99) "99+" else count.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                    ),
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}

private fun Screen.tabPresentation(s: Strings): Pair<ImageVector, String> = when (this) {
    Screen.Home -> Icons.Filled.Home to s.navHome
    Screen.TaskManagement -> Icons.AutoMirrored.Filled.List to s.navManage
    Screen.CompletedTask -> Icons.Filled.CheckCircle to s.navCompleted
    Screen.GroupHome -> Icons.Filled.Group to s.navGroups
    Screen.IncomingInvites -> Icons.Filled.Email to s.navInvites
    else -> Icons.Filled.Home to name
}

/**
 * The Groups tab routes to `NoGroup` when the user doesn't belong to any
 * group yet — that's the screen that prompts them to create / accept an
 * invite. Other tabs always navigate to themselves.
 */
private fun Screen.resolveTarget(): Screen =
    if (this == Screen.GroupHome && AuthState.groups.isEmpty()) Screen.NoGroup else this
