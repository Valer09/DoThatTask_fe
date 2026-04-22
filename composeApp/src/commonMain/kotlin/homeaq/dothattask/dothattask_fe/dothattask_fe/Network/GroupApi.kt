package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.CreateGroupRequest
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class GroupApi(private val client: HttpClient) {

    /** `POST /api/groups` — returns the created group (201) or a 409 message. */
    suspend fun create(name: String): ApiResult<GroupInfo> = try {
        val resp = client.post("/api/groups") {
            contentType(ContentType.Application.Json)
            setBody(CreateGroupRequest(name))
        }
        when (resp.status.value) {
            in 200..299 -> ApiResult.Success(resp.body())
            409 -> ApiResult.Error(resp.body<String>().ifBlank { "Cannot create group" })
            else -> ApiResult.Error("Create group failed (${resp.status.value})")
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }

    /**
     * `GET /api/groups/me` — returns 200 + [GroupInfo] when the caller is in a
     * group, or 204 No Content when they're not.
     */
    suspend fun myGroup(): ApiResult<GroupInfo?> = try {
        val resp = client.get("/api/groups/me")
        when (resp.status.value) {
            204 -> ApiResult.Success(null)
            in 200..299 -> ApiResult.Success(resp.body())
            else -> ApiResult.Error("Could not load group (${resp.status.value})")
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }

    /** `POST /api/groups/leave` — 200 on success, 409 if owner with members. */
    suspend fun leave(): ApiResult<String> = try {
        val resp = client.post("/api/groups/leave")
        when (resp.status.value) {
            in 200..299 -> ApiResult.Success(resp.body<String>().ifBlank { "Left group" })
            409 -> ApiResult.Error(resp.body<String>().ifBlank { "Cannot leave group" })
            404 -> ApiResult.NotFound(resp.body<String>().ifBlank { "You are not in a group" })
            else -> ApiResult.Error("Leave failed (${resp.status.value})")
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }
}
