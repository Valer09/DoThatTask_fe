package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class FcmTokenRequest(val token: String, val platform: String = "android")

class NotificationApi(private val client: HttpClient) {

    /**
     * `POST /api/notifications/register` — bind this device's FCM token to the
     * authenticated user. Idempotent on the server side.
     */
    suspend fun registerFcmToken(token: String, platform: String = "android"): ApiResult<Unit> = try {
        val resp = client.post("/api/notifications/register") {
            contentType(ContentType.Application.Json)
            setBody(FcmTokenRequest(token, platform))
        }
        if (resp.status.value in 200..299) ApiResult.Success(Unit)
        else ApiResult.Error("Register FCM token failed (${resp.status.value})")
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error", isNetwork = true)
    }

    /** `DELETE /api/notifications/register` — unregister a token (e.g. on logout). */
    suspend fun unregisterFcmToken(token: String): ApiResult<Unit> = try {
        val resp = client.delete("/api/notifications/register") {
            contentType(ContentType.Application.Json)
            setBody(FcmTokenRequest(token))
        }
        if (resp.status.value in 200..299) ApiResult.Success(Unit)
        else ApiResult.Error("Unregister FCM token failed (${resp.status.value})")
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error", isNetwork = true)
    }

    suspend fun ackReminder(): ApiResult<Unit> = try {
        val resp = client.post("/api/notifications/reminder/ack") {
            contentType(ContentType.Application.Json)
        }
        if (resp.status.value in 200..299) ApiResult.Success(Unit)
        else ApiResult.Error("Ack notification failed: (${resp.status.value})")
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error", isNetwork = true)
    }

    suspend fun reactivateNotification(): ApiResult<Unit> = try {
        val resp = client.post("/api/notifications/reactivate") {
            contentType(ContentType.Application.Json)
        }
        if (resp.status.value in 200..299) ApiResult.Success(Unit)
        else ApiResult.Error("Reactivation notification failed: (${resp.status.value})")
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error", isNetwork = true)
    }

}
