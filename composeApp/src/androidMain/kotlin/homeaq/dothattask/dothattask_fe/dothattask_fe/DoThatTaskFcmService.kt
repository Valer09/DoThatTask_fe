package homeaq.dothattask.dothattask_fe.dothattask_fe

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.NotificationApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Receives FCM messages and refreshed device tokens.
 *
 * Server-driven flow (replaces the old WorkManager-based daily worker):
 *   - On every fresh token, push it to the backend so it can target this device.
 *   - On every message, forward title/body to the system notification tray.
 *
 * The notification channel + permission check is done in MainActivity at app
 * launch; this service assumes both are already configured.
 */
class DoThatTaskFcmService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        // Best-effort: only the authenticated client will succeed; if the user
        // is logged out the call fails silently and the token is re-sent the
        // next time MainActivity refreshes it after login.
        scope.launch {
            runCatching { NotificationApi(client()).registerFcmToken(token) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val notif = message.notification
        val title = notif?.title ?: message.data["title"] ?: "DoThatTask"
        val body = notif?.body ?: message.data["body"] ?: return
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val notification = NotificationCompat.Builder(this, FCM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(FCM_NOTIFICATION_ID, notification)
    }

    companion object {
        const val FCM_CHANNEL_ID = "fcm_channel"
        const val FCM_CHANNEL_NAME = "Push notifications"
        private const val FCM_NOTIFICATION_ID = 2001

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    FCM_CHANNEL_ID, FCM_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT,
                )
                context.getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(channel)
            }
        }
    }
}
