package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.InviteApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.GroupBadge
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun InviteMemberPage() {
    var username by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val targetGroupId = AppState.inviteTargetGroupId ?: AuthState.activeGroupId
    val targetGroup = AuthState.groups.firstOrNull { it.id == targetGroupId }

    val inviteApi = remember { InviteApi(client()) }

    LoadingOverlay(isLoading = loading)

    Column(modifier = Modifier.padding(top = 40.dp).padding(horizontal = 30.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Invite a member",
                style = MaterialTheme.typography.headlineMedium,
                color = TaskUIHelper.getMarinerBlue(),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                onClick = { AppState.currentScreen = Screen.GroupHome },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
            ) { Text("Back") }
        }

        Spacer(Modifier.height(12.dp))
        if (targetGroup != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Inviting into ", color = Color.DarkGray)
                GroupBadge(targetGroup.name, targetGroup.color)
            }
            Spacer(Modifier.height(8.dp))
        }
        Text(
            "Enter the exact username of the person you want to invite.",
            color = Color.DarkGray,
        )

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it; usernameError = null; message = null },
            label = { Text("Username") },
            isError = usernameError != null,
            supportingText = usernameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val trimmed = username.trim()
                if (trimmed.isEmpty()) {
                    usernameError = "Username cannot be empty"
                    return@Button
                }
                val gid = targetGroupId
                if (gid == null) {
                    messageIsError = true
                    message = "No group selected"
                    return@Button
                }
                loading = true
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        when (val resp = inviteApi.sendInvite(gid, trimmed)) {
                            is ApiResult.Success -> {
                                messageIsError = false
                                message = "Invite sent to @${resp.data.inviteeUsername}"
                                username = ""
                            }
                            is ApiResult.NotFound -> {
                                messageIsError = true
                                message = resp.message
                            }
                            is ApiResult.Error -> if (!resp.routeIfNetwork()) {
                                messageIsError = true
                                message = resp.message
                            }
                            is ApiResult.Unauthorized -> {
                                message = "Unauthorized"
                                AppState.currentScreen = Screen.Login
                            }
                        }
                    } catch (e: Exception) {
                        messageIsError = true
                        message = e.message ?: "Invite failed"
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Hand, true).focusable(),
            colors = ButtonDefaults.buttonColors(
                containerColor = TaskUIHelper.getMarinerBlue(),
                contentColor = Color.White,
            ),
        ) {
            Text("Send invite")
        }

        message?.let {
            Spacer(Modifier.height(16.dp))
            Text(
                it,
                color = if (messageIsError) MaterialTheme.colorScheme.error else TaskUIHelper.getMarinerBlue(),
            )
        }
    }
}
