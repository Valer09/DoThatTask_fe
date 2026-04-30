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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.GroupApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createUnauthenticatedClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.GroupBadge
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.GroupCategoriesSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun GroupHomePage() {
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
            is ApiResult.Error -> error = resp.message
            is ApiResult.NotFound -> error = resp.message
            is ApiResult.Unauthorized -> {
                error = "Unauthorized"
                AppState.currentScreen = Screen.Login
            }
        }
        loading = false
    }

    LaunchedEffect(Unit) { reload() }

    if (loading && groups.isEmpty()) {
        Column(
            Modifier.fillMaxWidth().padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { CircularProgressIndicator() }
        return
    }

    Column(modifier = Modifier.padding(top = 24.dp).padding(horizontal = 24.dp).fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                "My groups",
                style = MaterialTheme.typography.headlineMedium,
                color = TaskUIHelper.getMarinerBlue(),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = { AppState.currentScreen = Screen.NoGroup },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskUIHelper.getMarinerBlue(),
                    contentColor = Color.White,
                ),
            ) { Text("+ Create group") }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(groups) { group ->
                val isOwner = (AuthState.username ?: "").equals(group.ownerUsername, ignoreCase = true)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            GroupBadge(group.name, group.color, fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "owned by @${group.ownerUsername}",
                                color = Color.DarkGray,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Members (${group.members.size})", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        group.members.forEach { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(TaskUIHelper.getLightGray())
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(m.name, fontWeight = FontWeight.Bold)
                                    Text("@${m.username}", color = Color.DarkGray)
                                }
                                Text(
                                    if (m.username.equals(group.ownerUsername, ignoreCase = true)) "owner"
                                    else m.role.name.lowercase(),
                                    color = TaskUIHelper.getMarinerBlue(),
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                        }

                        Spacer(Modifier.height(8.dp))
                        // Per-group categories editor — every member can add or
                        // unlink (the backend enforces blocking unlink while
                        // tasks still reference a category).
                        GroupCategoriesSection(groupId = group.id)

                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            if (isOwner) {
                                Button(
                                    onClick = {
                                        AppState.inviteTargetGroupId = group.id
                                        AppState.currentScreen = Screen.InviteMember
                                    },
                                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TaskUIHelper.getMarinerBlue(),
                                        contentColor = Color.White,
                                    ),
                                ) { Text("Invite member") }
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
                                            is ApiResult.Error -> message = resp.message
                                            is ApiResult.NotFound -> message = resp.message
                                            is ApiResult.Unauthorized -> {
                                                error = "Unauthorized"
                                                AppState.currentScreen = Screen.Login
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                            ) { Text("Leave") }
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
