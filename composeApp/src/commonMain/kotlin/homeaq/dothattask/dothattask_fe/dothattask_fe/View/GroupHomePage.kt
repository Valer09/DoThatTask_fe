package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupInfo
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.GroupApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createUnauthenticatedClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

@Composable
fun GroupHomePage() {
    var info by remember { mutableStateOf<GroupInfo?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    val groupApi = remember { GroupApi(createHttpClient()) }
    val authApi = remember { AuthApi(createUnauthenticatedClient(), createHttpClient()) }

    suspend fun reload() {
        loading = true
        error = null
        when (val resp = groupApi.myGroup()) {
            is ApiResult.Success -> {
                info = resp.data
                if (resp.data == null) AppState.currentScreen = Screen.NoGroup
            }
            is ApiResult.Error -> error = resp.message
            is ApiResult.NotFound -> error = resp.message
        }
        loading = false
    }

    LaunchedEffect(Unit) { reload() }

    if (loading && info == null) {
        Column(
            Modifier.fillMaxWidth().padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { CircularProgressIndicator() }
        return
    }

    val group = info ?: run {
        Text("No group.")
        return
    }
    val isOwner = (AuthState.username ?: "").equals(group.ownerUsername, ignoreCase = true)

    Column(modifier = Modifier.padding(top = 24.dp).padding(horizontal = 24.dp)) {
        Text(
            group.name,
            style = MaterialTheme.typography.headlineMedium,
            color = TaskUIHelper.getMarinerBlue(),
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text("Owner: ${group.ownerUsername}", color = Color.DarkGray)

        Spacer(Modifier.height(20.dp))
        Text("Members (${group.members.size})", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth().height(240.dp)) {
            items(group.members) { m ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TaskUIHelper.getLightGray())
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(m.name, fontWeight = FontWeight.Bold)
                        Text("@${m.username}", color = Color.DarkGray)
                    }
                    Text(
                        if (m.username.equals(group.ownerUsername, ignoreCase = true)) "owner" else m.role.name.lowercase(),
                        color = TaskUIHelper.getMarinerBlue(),
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        Spacer(Modifier.height(20.dp))
        Row {
            if (isOwner) {
                Button(
                    onClick = { AppState.currentScreen = Screen.InviteMember },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskUIHelper.getMarinerBlue(),
                        contentColor = Color.White,
                    ),
                ) { Text("Invite member") }
                Spacer(Modifier.width(12.dp))
            }
            OutlinedButton(
                onClick = { AppState.currentScreen = Screen.IncomingInvites },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
            ) { Text("My invites") }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        when (val resp = groupApi.leave()) {
                            is ApiResult.Success -> {
                                // After leaving, refresh to drop gid from the JWT.
                                authApi.refresh()
                                AuthState.groupId = null
                                AuthState.persist()
                                AppState.currentScreen = Screen.NoGroup
                            }
                            is ApiResult.Error -> message = resp.message
                            is ApiResult.NotFound -> message = resp.message
                        }
                    }
                },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
            ) { Text("Leave group") }
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
