package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.AppLocale
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocalStrings
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n.LocaleManager
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createUnauthenticatedClient
import kotlinx.coroutines.launch

/**
 * Account / app settings hub. Reached by tapping the avatar in the header.
 *
 * Shows the signed-in user up top (avatar + display name + email/username),
 * then a list of actions grouped by section (Account, Preferences, Session).
 * Tapping a row navigates to the dedicated screen (e.g. Change password)
 * or runs the action inline (Logout). The language picker lives here too —
 * picking one immediately recomposes the whole tree thanks to
 * [LocalStrings] / [LocaleManager].
 */
@Composable
fun SettingsPage(onLogout: () -> Unit) {
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    val authApi = remember { AuthApi(createUnauthenticatedClient(), client()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = { AppState.changePage(Screen.Home) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .pointerHoverIcon(PointerIcon.Hand, true),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = s.back,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = s.titleSettings,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(16.dp))

        // Identity card — quick reminder of who you're signed in as.
        ProfileCard()

        Spacer(Modifier.height(20.dp))

        SectionLabel(s.settingsSectionAccount)
        Spacer(Modifier.height(6.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = TaskUIHelper.appCardShape(),
            colors = TaskUIHelper.appCardColors(),
        ) {
            SettingsRow(
                icon = Icons.Filled.Lock,
                label = s.headerChangePassword,
                onClick = { AppState.changePage(Screen.ChangePassword) },
            )
        }

        Spacer(Modifier.height(20.dp))

        SectionLabel(s.settingsSectionPreferences)
        Spacer(Modifier.height(6.dp))
        LanguagePickerCard()

        Spacer(Modifier.height(20.dp))

        SectionLabel(s.settingsSectionSession)
        Spacer(Modifier.height(6.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = TaskUIHelper.appCardShape(),
            colors = TaskUIHelper.appCardColors(),
        ) {
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                label = s.headerLogout,
                tint = MaterialTheme.colorScheme.error,
                showChevron = false,
                onClick = {
                    scope.launch {
                        runCatching { authApi.logout() }
                        onLogout()
                    }
                },
            )
        }
    }
}

@Composable
private fun ProfileCard() {
    val initial = (AuthState.displayName ?: AuthState.username)
        ?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = TaskUIHelper.appCardShape(),
        colors = TaskUIHelper.appCardColors(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initial,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = AuthState.displayName ?: AuthState.username ?: "—",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
                val sub = AuthState.email?.takeIf { it.isNotBlank() } ?: AuthState.username
                if (!sub.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = sub,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguagePickerCard() {
    val s = LocalStrings.current
    val current = LocaleManager.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = TaskUIHelper.appCardShape(),
        colors = TaskUIHelper.appCardColors(),
    ) {
        Column {
            // Section header row, mirrors the icon-leading style of
            // [SettingsRow] without a chevron — no navigation here, the two
            // language rows below are the actual choices.
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Translate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = s.headerLanguage,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            LanguageOptionRow(
                flag = "🇬🇧",
                label = s.headerLanguageEnglish,
                selected = current == AppLocale.English,
                onClick = { LocaleManager.set(AppLocale.English) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            LanguageOptionRow(
                flag = "🇮🇹",
                label = s.headerLanguageItalian,
                selected = current == AppLocale.Italian,
                onClick = { LocaleManager.set(AppLocale.Italian) },
            )
        }
    }
}

@Composable
private fun LanguageOptionRow(
    flag: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .pointerHoverIcon(PointerIcon.Hand, true)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(text = flag, fontSize = 20.sp)
            Spacer(Modifier.width(14.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    showChevron: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .pointerHoverIcon(PointerIcon.Hand, true)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = label,
                color = tint,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}
