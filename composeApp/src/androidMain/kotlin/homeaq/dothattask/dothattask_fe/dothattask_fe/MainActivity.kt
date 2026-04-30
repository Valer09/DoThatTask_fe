package homeaq.dothattask.dothattask_fe.dothattask_fe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthProvider
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.NotificationApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.App
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AuthProvider.init(applicationContext)

        DoThatTaskFcmService.ensureChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ensurePostNotificationsPermission()
        } else {
            registerFcmToken()
        }

        handleNotificationIntent(intent)

        setContent { App( onLoginSuccess = {
            registerFcmToken()
        }) }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch{
            runCatching { registerFcmToken() }
            runCatching { NotificationApi(client()).reactivateNotification() }
        }
    }


    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) registerFcmToken()
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun ensurePostNotificationsPermission() {
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) registerFcmToken()
        else requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /**
     * Best-effort: fetch the FCM token and ship it to the backend so the
     * server can target this device. Failures are logged on the server side
     * (no auth → drop) and re-tried automatically by [DoThatTaskFcmService.onNewToken]
     * the next time FCM rotates the token.
     */
    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result ?: return@addOnCompleteListener
            lifecycleScope.launch {
                runCatching { NotificationApi(client()).registerFcmToken(token) }
            }
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val type = intent?.getStringExtra("type") ?: return
        val targetId = intent.getStringExtra("targetId")

        when (type) {
            //"group_invitation" -> if (targetId != null) Screen.TaskDetail(targetId) else Screen.Home
            "task_reminder" ->
                {
                lifecycleScope.launch {
                    runCatching { NotificationApi(client()).ackReminder() }
                }
                    AppState.currentScreen = Screen.Home
            }
            "group_invitation" -> AppState.currentScreen = Screen.IncomingInvites

            else -> AppState.currentScreen = Screen.Home
        }
    }
}


