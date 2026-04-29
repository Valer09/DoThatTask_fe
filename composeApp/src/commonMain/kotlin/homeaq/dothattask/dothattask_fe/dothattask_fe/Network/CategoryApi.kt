package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(val name: String, val color: String? = null)

class CategoryApi(private val client: HttpClient) {

    /** `GET /api/categories` — categories visible to [groupId]. */
    suspend fun list(groupId: Int): ApiResult<List<TaskCategory>> = try {
        val resp = client.get("/api/categories") { withGroup(groupId) }
        if (resp.status.value in 200..299) ApiResult.Success(resp.body())
        else ApiResult.Error("Could not load categories (${resp.status.value})")
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }

    /**
     * `POST /api/categories` — create-or-link. If a category with the same
     * (case-insensitive) name already exists, the response just links it to
     * the group; otherwise a new global category is created with the given
     * (or palette-picked) color.
     */
    suspend fun create(groupId: Int, name: String, color: String?): ApiResult<TaskCategory> = try {
        val resp = client.post("/api/categories") {
            withGroup(groupId)
            contentType(ContentType.Application.Json)
            setBody(CreateCategoryRequest(name, color))
        }
        when (resp.status.value) {
            in 200..299 -> ApiResult.Success(resp.body())
            400 -> ApiResult.Error(resp.body<String>().ifBlank { "Invalid category" })
            else -> ApiResult.Error("Create category failed (${resp.status.value})")
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }

    /**
     * `DELETE /api/categories/{id}` — only unlinks from the group, never
     * deletes the global category. Returns 409 if any task in this group
     * still uses the category.
     */
    suspend fun unlink(groupId: Int, categoryId: Int): ApiResult<Unit> = try {
        val resp = client.delete("/api/categories/$categoryId") {
            withGroup(groupId)
        }
        when (resp.status.value) {
            in 200..299 -> ApiResult.Success(Unit)
            409 -> ApiResult.Error(resp.body<String>().ifBlank { "Category in use" })
            404 -> ApiResult.NotFound(resp.body<String>().ifBlank { "Not linked to this group" })
            else -> ApiResult.Error("Unlink failed (${resp.status.value})")
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }
}
