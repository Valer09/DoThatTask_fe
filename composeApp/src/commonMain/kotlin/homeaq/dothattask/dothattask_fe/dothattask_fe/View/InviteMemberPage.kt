package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.InviteApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.GroupBadge
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

private val EmailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")

@Composable
@Preview
fun InviteMemberPage() {
    var userEmail by remember { mutableStateOf("") }
    var userEmailError by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val targetGroupId = AppState.inviteTargetGroupId ?: AuthState.activeGroupId
    val targetGroup = AuthState.groups.firstOrNull { it.id == targetGroupId }

    val inviteApi = remember { InviteApi(client()) }

    LoadingOverlay(isLoading = loading)

    Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp).padding(horizontal = 20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = TaskUIHelper.appCardShape(),
            colors = TaskUIHelper.appCardColors(),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Invite a member",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
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
                        Text(
                            "Inviting into ",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        )
                        GroupBadge(targetGroup.name, targetGroup.color)
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    "Enter the email of the person you want to invite. " +
                            "They must already have an account.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                )

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = userEmail,
                    onValueChange = {
                        userEmail = it.filter { ch -> !ch.isWhitespace() }
                        userEmailError = null
                        message = null
                    },
                    label = { Text("Email") },
                    colors = TaskUIHelper.appTextFieldColors(),
                    isError = userEmailError != null,
                    supportingText = userEmailError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                        .semantics { contentType = ContentType.EmailAddress },
                    singleLine = true,
                )

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        val trimmed = userEmail.trim()
                        userEmailError = when {
                            trimmed.isEmpty() -> "Email cannot be empty"
                            trimmed.length > 320 -> "Email is too long"
                            !EmailRegex.matches(trimmed) -> "Enter a valid email address"
                            else -> null
                        }
                        if (userEmailError != null) return@Button

                        val gid = targetGroupId ?: run {
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
                                        message = "Invite sent to ${resp.data.inviteeEmail}"
                                        userEmail = ""
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
                                    is ApiResult.Forbidden -> {
                                        message = "Forbidden"
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
                        containerColor = TaskUIHelper.getComplementary(),
                        contentColor = Color.Black,
                    ),
                ) {
                    Text("Send invite")
                }

                message?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        it,
                        color = if (messageIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}