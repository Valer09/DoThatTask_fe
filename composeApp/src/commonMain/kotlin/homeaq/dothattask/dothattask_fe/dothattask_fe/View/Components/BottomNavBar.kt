package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * Hand-rolled on top of a [Surface] + [Row] instead of Material's
 * [androidx.compose.material3.NavigationBar], which has a fixed 80dp
 * height and a wide icon→label gap. Here the items are 22dp icons with
 * 2dp of breathing room above the 10sp label, all inside a 60dp pill.
 *
 * Each tab uses **outlined** icons when inactive and **rounded-filled**
 * icons when active — the same pattern Revolut and Instagram use.
 *
 * The whole bar is lifted above the Android gesture-nav inset via
 * [windowInsetsPadding] so it never sits underneath the system bar on
 * edge-to-edge devices.
 */
@Composable
fun BottomNavBar() {
    val selectedTopLevel = AppState.currentScreen.topLevel()
    val s = LocalStrings.current

    Surface(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            bottomNavDestinations.forEach { screen ->
                NavItem(
                    screen = screen,
                    selected = selectedTopLevel == screen,
                    s = s,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    screen: Screen,
    selected: Boolean,
    s: Strings,
    modifier: Modifier,
) {
    val (icon, label) = screen.tabPresentation(s, selected)
    val fg = if (selected) MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
    val pillColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
    else Color.Transparent

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { AppState.changePage(screen.resolveTarget()) }
            .pointerHoverIcon(PointerIcon.Hand, true)
            .background(pillColor)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (screen == Screen.IncomingInvites) {
                BadgedBox(
                    badge = {
                        val count = NotificationCenter.pendingInvitesCount
                        if (count > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
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
                        tint = fg,
                        modifier = Modifier.size(22.dp),
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = fg,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                color = fg,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

private fun Screen.tabPresentation(s: Strings, selected: Boolean): Pair<ImageVector, String> = when (this) {
    Screen.Home -> (if (selected) Icons.Rounded.Home else Icons.Outlined.Home) to s.navHome
    Screen.TaskManagement -> (if (selected) Icons.AutoMirrored.Rounded.List else Icons.AutoMirrored.Outlined.List) to s.navManage
    Screen.CompletedTask -> (if (selected) Icons.Rounded.TaskAlt else Icons.Outlined.TaskAlt) to s.navCompleted
    Screen.GroupHome -> (if (selected) Icons.Rounded.Group else Icons.Outlined.Group) to s.navGroups
    Screen.IncomingInvites -> (if (selected) Icons.Rounded.Mail else Icons.Outlined.Mail) to s.navInvites
    else -> Icons.Rounded.Home to name
}

/**
 * The Groups tab routes to `NoGroup` when the user doesn't belong to any
 * group yet — that's the screen that prompts them to create / accept an
 * invite. Other tabs always navigate to themselves.
 */
private fun Screen.resolveTarget(): Screen =
    if (this == Screen.GroupHome && AuthState.groups.isEmpty()) Screen.NoGroup else this
