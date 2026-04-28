package homeaq.dothattask.dothattask_fe.dothattask_fe

import android.Manifest
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
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthProvider
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.NotificationApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.App
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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

        setContent { App() }
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
                runCatching { NotificationApi(createHttpClient()).registerFcmToken(token) }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
