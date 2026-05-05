package homeaq.dothattask.dothattask_fe.dothattask_fe.View

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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.Invite
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.InviteApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createUnauthenticatedClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.GroupBadge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun IncomingInvitesPage() {
    var invites by remember { mutableStateOf<List<Invite>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    val inviteApi = remember { InviteApi(client()) }
    val authApi = remember { AuthApi(createUnauthenticatedClient(), client()) }

    suspend fun reload() {
        loading = true
        error = null
        when (val resp = inviteApi.incoming()) {
            is ApiResult.Success -> invites = resp.data
            is ApiResult.Error -> if (!resp.routeIfNetwork()) error = resp.message
            is ApiResult.NotFound -> error = resp.message
            is ApiResult.Unauthorized -> {
                error = "Unauthorized"
                AppState.currentScreen = Screen.Login
            }
            is ApiResult.Forbidden -> {
                error = "Forbidden"
            }
        }
        loading = false
    }

    LaunchedEffect(Unit) { reload() }

    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp).padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Incoming invites",
                style = MaterialTheme.typography.headlineMedium,
                color = onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                onClick = {
                    AppState.currentScreen =
                        if (AuthState.groups.isNotEmpty()) Screen.GroupHome else Screen.NoGroup
                },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
            ) { Text("Back") }
        }

        Spacer(Modifier.height(12.dp))

        if (loading) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            return
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            return
        }
        if (invites.isEmpty()) {
            Text("No pending invites.", color = onSurface.copy(alpha = 0.7f))
            return
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(invites) { invite ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = TaskUIHelper.appCardShape(),
                    colors = TaskUIHelper.appCardColors(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        GroupBadge(invite.groupName, invite.groupColor)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Invited by @${invite.inviterUsername}",
                            color = onSurface.copy(alpha = 0.75f),
                        )
                        Spacer(Modifier.height(12.dp))
                        Row {
                            Button(
                                onClick = {
                                    CoroutineScope(Dispatchers.Default).launch {
                                        when (val resp = inviteApi.accept(invite.id)) {
                                            is ApiResult.Success -> {
                                                authApi.refresh()
                                                reload()
                                            }
                                            is ApiResult.Error -> if (!resp.routeIfNetwork()) message = resp.message
                                            is ApiResult.NotFound -> message = resp.message
                                            is ApiResult.Unauthorized -> {
                                                error = "Unauthorized"
                                                AppState.currentScreen = Screen.Login
                                            }
                                            is ApiResult.Forbidden -> {
                                                error = "Forbidden"
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TaskUIHelper.getComplementary(),
                                    contentColor = Color.Black,
                                ),
                            ) { Text("Accept") }
                            Spacer(Modifier.width(10.dp))
                            OutlinedButton(
                                onClick = {
                                    CoroutineScope(Dispatchers.Default).launch {
                                        when (val resp = inviteApi.reject(invite.id)) {
                                            is ApiResult.Success -> reload()
                                            is ApiResult.Error -> if (!resp.routeIfNetwork()) message = resp.message
                                            is ApiResult.NotFound -> message = resp.message
                                            is ApiResult.Unauthorized -> {
                                                error = "Unauthorized"
                                                AppState.currentScreen = Screen.Login
                                            }
                                            is ApiResult.Forbidden -> {
                                                error = "Forbidden"
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                            ) { Text("Reject") }
                        }
                    }
                }
            }
        }

        message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
