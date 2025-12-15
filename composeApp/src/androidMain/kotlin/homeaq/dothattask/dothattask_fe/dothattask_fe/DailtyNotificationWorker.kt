package homeaq.dothattask.dothattask_fe.dothattask_fe

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthProvider
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient


class DailyNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {

        val sentCount = NotificationPrefs.getSentCount(applicationContext)
        val hasOpenedApp = NotificationPrefs.hasOpenedApp(applicationContext)

        // 🔕 Anti-spam rule
        if (!hasOpenedApp && sentCount >= 3) {
            return Result.success()
        }

        if(AuthProvider.getToken().isNullOrEmpty()) return Result.retry()

        val tasks = TaskApi(createHttpClient()).getAssignedTask()
        when (tasks) {
            is ApiResult.NotFound -> {
                showNotification("Hey! Don't you want to pick a new task for the week? 👻💪💪")
                NotificationPrefs.incrementSent(applicationContext)
                return Result.success()
            }

            is ApiResult.Success -> {
                showNotification("Ehy don't forget about your weekly task! 👀")
                NotificationPrefs.incrementSent(applicationContext)
                return Result.success()
            }

            else -> return Result.retry()
        }
    }


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(text: String)
    {
        val notification = NotificationCompat.Builder(
            applicationContext,
            "daily_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Daily Reminder")
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat
            .from(applicationContext)
            .notify(1001, notification)
    }
}
