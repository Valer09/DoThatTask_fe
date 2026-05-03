package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupSummary
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.GroupApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createUnauthenticatedClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun NoGroupPage() {
    var groupName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val groupApi = remember { GroupApi(client()) }
    val authApi = remember { AuthApi(createUnauthenticatedClient(), client()) }

    LoadingOverlay(isLoading = loading)

    Column(modifier = Modifier.padding(top = 40.dp).padding(horizontal = 30.dp)) {
        val title = if (AuthState.groups.isEmpty()) "You're not in a group yet" else "Create another group"
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            color = TaskUIHelper.getPrimary(),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.height(12.dp))
        Text(
            "Create a new group to start assigning tasks, or wait for an " +
                "incoming invite from someone who already has one.",
            color = Color.DarkGray,
        )

        Spacer(Modifier.height(24.dp))
        Text("Create a group", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it; nameError = null; message = null },
            label = { Text("Group name") },
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(Modifier.height(8.dp))
        Row {
            Button(
                onClick = {
                    val trimmed = groupName.trim()
                    if (trimmed.isEmpty()) {
                        nameError = "Group name cannot be empty"
                        return@Button
                    }
                    loading = true
                    CoroutineScope(Dispatchers.Default).launch {
                        try {
                            when (val resp = groupApi.create(trimmed)) {
                                is ApiResult.Success -> {
                                    val created = resp.data
                                    AuthState.groups = AuthState.groups + GroupSummary(
                                        id = created.id,
                                        name = created.name,
                                        color = created.color,
                                    )
                                    AuthState.activeGroupId = created.id
                                    // Refresh tokens so the persisted session
                                    // reflects the new group list.
                                    authApi.refresh()
                                    AppState.currentScreen = Screen.GroupHome
                                }
                                is ApiResult.Error -> if (!resp.routeIfNetwork()) {
                                    messageIsError = true
                                    message = resp.message
                                }
                                is ApiResult.NotFound -> {
                                    messageIsError = true
                                    message = "Group endpoint unavailable"
                                }
                                is ApiResult.Unauthorized -> {
                                    messageIsError = true
                                    message = "Unauthorized"
                                    AppState.currentScreen = Screen.Login
                                }
                            }
                        } catch (e: Exception) {
                            messageIsError = true
                            message = e.message ?: "Create group failed"
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskUIHelper.getPrimary(),
                    contentColor = Color.White,
                ),
            ) {
                Text("Create group")
            }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(
                onClick = { AppState.currentScreen = Screen.IncomingInvites },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true),
            ) {
                Text("See incoming invites")
            }
        }

        message?.let {
            Spacer(Modifier.height(16.dp))
            Text(
                it,
                color = if (messageIsError) MaterialTheme.colorScheme.error else TaskUIHelper.getPrimary(),
            )
        }
    }
}
