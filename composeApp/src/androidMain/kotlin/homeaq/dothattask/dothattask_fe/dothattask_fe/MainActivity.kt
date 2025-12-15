package homeaq.dothattask.dothattask_fe.dothattask_fe

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthProvider


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AuthProvider.init(applicationContext)

        NotificationPrefs.markAppOpened(this)
        createNotificationChannel(this)
        checkAndRequestNotificationPermission(this)

        setContent { App() }
    }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        {
            granted -> if (granted) scheduleDailyWorker(this)
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkAndRequestNotificationPermission(context: Context)
    {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS  ) == PackageManager.PERMISSION_GRANTED)
            scheduleDailyWorker(context)

        else requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    fun scheduleDailyWorker(context: Context)
    {
        val constraints =  Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val request =
            PeriodicWorkRequestBuilder<DailyNotificationWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "daily_notification_worker",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

object NotificationPrefs {

    private const val PREFS = "notification_prefs"
    private const val KEY_SENT_COUNT = "sent_count"
    private const val KEY_APP_OPENED = "app_opened"

    fun incrementSent(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit { putInt(KEY_SENT_COUNT, getSentCount(context) + 1) }
    }

    fun getSentCount(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_SENT_COUNT, 0)

    fun markAppOpened(context: Context)
    {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit{ putBoolean(KEY_APP_OPENED, true)     }
    }

    fun hasOpenedApp(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_APP_OPENED, false)
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "daily_channel",
            "Daily reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}






