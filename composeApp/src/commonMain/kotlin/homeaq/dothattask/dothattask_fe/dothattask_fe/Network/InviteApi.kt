package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.Invite
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.SendInviteRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class InviteApi(private val client: HttpClient) {

    /**
     * `POST /api/invites` — owner-only. The [groupId] tells the server which
     * of the inviter's groups the invite is for.
     */
    suspend fun sendInvite(groupId: Int, inviteeUsername: String): ApiResult<Invite> = try {
        val resp = client.post("/api/invites") {
            withGroup(groupId)
            contentType(ContentType.Application.Json)
            setBody(SendInviteRequest(inviteeUsername))
        }
        when (resp.status.value) {
            in 200..299 -> ApiResult.Success(resp.body())
            404 -> ApiResult.NotFound(resp.body<String>().ifBlank { "User not found" })
            403 -> ApiResult.Error(resp.body<String>().ifBlank { "Forbidden" })
            409 -> ApiResult.Error(resp.body<String>().ifBlank { "Cannot invite this user" })
            else -> ApiResult.Error("Invite failed (${resp.status.value})")
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }

    /** `GET /api/invites/incoming` — list PENDING invites for the caller. */
    suspend fun incoming(): ApiResult<List<Invite>> = try {
        val resp = client.get("/api/invites/incoming")
        when (resp.status.value) {
            in 200..299 -> ApiResult.Success(resp.body())
            else -> ApiResult.Error("Could not load invites (${resp.status.value})")
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }

    suspend fun accept(inviteId: Int): ApiResult<Invite> = try {
        val resp = client.post("/api/invites/$inviteId/accept")
        when (resp.status.value) {
            in 200..299 -> ApiResult.Success(resp.body())
            404 -> ApiResult.NotFound(resp.body<String>().ifBlank { "Invite not found" })
            403 -> ApiResult.Error(resp.body<String>().ifBlank { "Forbidden" })
            409 -> ApiResult.Error(resp.body<String>().ifBlank { "Invite is no longer pending" })
            else -> ApiResult.Error("Accept failed (${resp.status.value})")
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }

    suspend fun reject(inviteId: Int): ApiResult<Invite> = try {
        val resp = client.post("/api/invites/$inviteId/reject")
        when (resp.status.value) {
            in 200..299 -> ApiResult.Success(resp.body())
            404 -> ApiResult.NotFound(resp.body<String>().ifBlank { "Invite not found" })
            409 -> ApiResult.Error(resp.body<String>().ifBlank { "Invite is no longer pending" })
            else -> ApiResult.Error("Reject failed (${resp.status.value})")
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }

    /** Owner-only: mark a PENDING invite as REVOKED. */
    suspend fun revoke(inviteId: Int): ApiResult<Invite> = try {
        val resp = client.delete("/api/invites/$inviteId")
        when (resp.status.value) {
            in 200..299 -> ApiResult.Success(resp.body())
            404 -> ApiResult.NotFound(resp.body<String>().ifBlank { "Invite not found" })
            403 -> ApiResult.Error(resp.body<String>().ifBlank { "Forbidden" })
            409 -> ApiResult.Error(resp.body<String>().ifBlank { "Invite is no longer pending" })
            else -> ApiResult.Error("Revoke failed (${resp.status.value})")
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }
}
