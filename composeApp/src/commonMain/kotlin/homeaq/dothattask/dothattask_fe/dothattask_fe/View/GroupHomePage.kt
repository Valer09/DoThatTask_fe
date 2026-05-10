package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupInfo
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupSummary
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocalStrings
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.GroupApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createUnauthenticatedClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.GroupBadge
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.GroupCategoriesSection
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.appButtonSizeSmall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun GroupHomePage() {
    val s = LocalStrings.current
    var groups by remember { mutableStateOf<List<GroupInfo>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    val groupApi = remember { GroupApi(client()) }
    val authApi = remember { AuthApi(createUnauthenticatedClient(), client()) }

    suspend fun reload() {
        loading = true
        error = null
        when (val resp = groupApi.myGroups()) {
            is ApiResult.Success -> {
                groups = resp.data
                if (resp.data.isEmpty()) AppState.currentScreen = Screen.NoGroup
                AuthState.groups = resp.data.map { GroupSummary(it.id, it.name, it.color) }
                if (AuthState.activeGroupId == null || AuthState.groups.none { it.id == AuthState.activeGroupId }) {
                    AuthState.activeGroupId = AuthState.groups.firstOrNull()?.id
                }
            }
            is ApiResult.Error -> if (!resp.routeIfNetwork()) error = resp.message
            is ApiResult.NotFound -> error = resp.message
            is ApiResult.Unauthorized -> {
                error = s.unauthorized
                AppState.currentScreen = Screen.Login
            }
            is ApiResult.Forbidden -> {
            error = s.forbidden
            }
        }
        loading = false
    }

    LaunchedEffect(Unit) { reload() }

    if (loading && groups.isEmpty()) {
        Column(
            Modifier.fillMaxWidth().padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
        return
    }

    val onSurface = MaterialTheme.colorScheme.onSurface
    val memberRowBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(top = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                s.groupsTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = { AppState.currentScreen = Screen.NoGroup },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskUIHelper.getComplementary(),
                    contentColor = Color.Black,
                ),
            ) { Text(s.groupsCreateGroup) }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(groups) { group ->
                val isOwner = (AuthState.email ?: "").equals(group.ownerEmail, ignoreCase = true)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = TaskUIHelper.appCardShape(),
                    colors = TaskUIHelper.appCardColors(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            GroupBadge(group.name, group.color, fontSize = 17.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${s.groupsOwnedBy} ${group.ownerEmail}",
                                color = onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "${s.groupsMembers} (${group.members.size})",
                            fontWeight = FontWeight.SemiBold,
                            color = onSurface,
                        )
                        Spacer(Modifier.height(4.dp))
                        group.members.forEach { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .background(memberRowBg, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(m.name, fontWeight = FontWeight.Bold, color = onSurface)
                                    // Email is the canonical key now — show it
                                    // as the secondary identifier; the legacy
                                    // username only surfaces if the email is
                                    // empty (un-migrated row).
                                    val secondary = m.email.takeIf { it.isNotBlank() } ?: m.username.orEmpty()
                                    if (secondary.isNotBlank()) {
                                        Text(secondary, color = onSurface.copy(alpha = 0.7f))
                                    }
                                }
                                Text(
                                    if (m.email.equals(group.ownerEmail, ignoreCase = true)) s.groupsRoleOwner
                                    else when (m.role.name.lowercase()) {
                                        "admin" -> s.groupsRoleAdmin
                                        else -> s.groupsRoleMember
                                    },
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        GroupCategoriesSection(groupId = group.id)

                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            if (isOwner) {
                                Button(
                                    onClick = {
                                        AppState.inviteTargetGroupId = group.id
                                        AppState.currentScreen = Screen.InviteMember
                                    },
                                    modifier = Modifier.appButtonSizeSmall().pointerHoverIcon(PointerIcon.Hand, true),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TaskUIHelper.getSecondary(),
                                        contentColor = TaskUIHelper.getAlternativeText(),
                                    ),
                                ) { Text(s.groupsInviteMember) }
                                Spacer(Modifier.width(8.dp))
                            }
                            OutlinedButton(
                                onClick = {
                                    CoroutineScope(Dispatchers.Default).launch {
                                        when (val resp = groupApi.leave(group.id)) {
                                            is ApiResult.Success -> {
                                                authApi.refresh()
                                                reload()
                                            }
                                            is ApiResult.Error -> if (!resp.routeIfNetwork()) message = resp.message
                                            is ApiResult.NotFound -> message = resp.message
                                            is ApiResult.Unauthorized -> {
                                                error = s.unauthorized
                                                AppState.currentScreen = Screen.Login
                                            }
                                            is ApiResult.Forbidden -> {
                                                error = s.forbidden
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.appButtonSizeSmall().pointerHoverIcon(PointerIcon.Hand, true),
                            ) { Text(s.groupsLeave) }
                        }
                    }
                }
            }
        }

        message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
