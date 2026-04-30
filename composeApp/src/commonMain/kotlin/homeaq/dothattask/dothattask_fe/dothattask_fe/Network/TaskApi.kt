package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskUpdate
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TaskApi(private val httpClient: HttpClient) {


    suspend fun removeTask(task: Task): ApiResult<String> {
        return try
        {
            val response = httpClient.delete("api/tasks/${task.name}") {
                withGroup(task.groupId)
            }

            when (response.status.value) {
                in 200..299 -> ApiResult.Success("Task deleted successfully")
                403 -> ApiResult.Error(
                    runCatching { response.body<String>() }.getOrNull()?.ifBlank { null }
                        ?: "Only the task creator can delete it",
                )
                else -> ApiResult.Error(response.call.response.status.toString())
            }
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    /** Creates a task in [groupId] (which the caller must belong to). */
    suspend fun createTask(task: Task, groupId: Int) : ApiResult<Task>
    {
        return try
        {
            val taskUpdate = TaskUpdate(
                "",
                task.name,
                task.description,
                task.category,
                task.status,
                task.ownership_username
            )
            val response = httpClient.post("/api/tasks")
            {
                withGroup(groupId)
                contentType(ContentType.Application.Json)
                setBody(taskUpdate)
            }

            if (response.status.value in 200..299) ApiResult.Success(response.body())
            else ApiResult.Error(response.call.response.status.toString())
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun updateTask(oldTask: Task, newTask: Task): ApiResult<Task> {
        return try
        {
            val taskUpdate = TaskUpdate(
                oldTask.name,
                newTask.name,
                newTask.description,
                newTask.category,
                newTask.status,
                newTask.ownership_username
            )
            val response = httpClient.post("/api/tasks")
            {
                withGroup(oldTask.groupId)
                contentType(ContentType.Application.Json)
                setBody(taskUpdate)
            }

            when (response.status.value) {
                in 200..299 -> ApiResult.Success(response.body())
                403 -> ApiResult.Error(
                    runCatching { response.body<String>() }.getOrNull()?.ifBlank { null }
                        ?: "Only the task creator can modify it",
                )
                else -> ApiResult.Error(response.call.response.status.toString())
            }
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    /** Fetches the task currently assigned to the caller in [groupId]. */
    suspend fun getAssignedTask(groupId: Int): ApiResult<Task>
    {
        return try
        {
            val response = httpClient.get("/api/tasks/assignedTask") {
                withGroup(groupId)
            }

            when (response.status.value) {
                in 200..299 -> ApiResult.Success(response.body())
                404 -> ApiResult.NotFound(response.body())
                else -> ApiResult.Error(response.call.response.status.toString())
            }
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    /** Picks a random task in [groupId] within [category] for the caller. */
    suspend fun pickTask(groupId: Int, category: TaskCategory): ApiResult<Task>
    {
        return try
        {
            val response = httpClient.post("/api/tasks/pickTask")
            {
                withGroup(groupId)
                url{
                    parameters.append("category", category.name)
                }
                contentType(ContentType.Application.Json)
            }

            when (response.status.value) {
                in 200..299 -> ApiResult.Success(response.body())
                404 -> ApiResult.NotFound(response.body())
                else -> ApiResult.Error(response.call.response.status.toString())
            }
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun unassignTask(task: Task):  ApiResult<Task>
    {
        return try
        {
            val response = httpClient.post("/api/tasks/unassign")
            {
                withGroup(task.groupId)
                url{
                    parameters.append("task_name", task.name)
                }
                contentType(ContentType.Application.Json)
            }

            when (response.status.value) {
                in 200..299 -> ApiResult.Success(task)
                404 -> ApiResult.NotFound(response.body())
                403 -> ApiResult.Error(
                    runCatching { response.body<String>() }.getOrNull()?.ifBlank { null }
                        ?: "Only the task creator can unassign it",
                )
                else -> ApiResult.Error(response.call.response.status.toString())
            }
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun completeTask(assignedTask: Task?) : ApiResult<String>
    {
        return try
        {
            if(assignedTask == null || assignedTask.name.isEmpty()) return ApiResult.Error("No task assigned. Error on the client")
            val response = httpClient.post("api/tasks/completeTask") {
                withGroup(assignedTask.groupId)
                url{
                    parameters.append("task_name", assignedTask.name)
                }
                contentType(ContentType.Application.Json)
            }
            if (response.status.value in 200..299) ApiResult.Success("Task completed successfully")
            else if (response.status.value == 404) ApiResult.NotFound("Task not found")
            else return ApiResult.Error(response.call.response.status.toString())
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Search tasks within a group. Always excludes tasks assigned to the
     * caller (the "secret task" rule). Pass null/empty to omit a filter.
     */
    suspend fun searchTasks(
        groupId: Int,
        creator: String? = null,
        category: TaskCategory? = null,
        assignee: String? = null,
    ): ApiResult<List<Task>> {
        return try {
            val response = httpClient.get("/api/tasks") {
                withGroup(groupId)
                url {
                    creator?.takeIf { it.isNotBlank() }?.let { parameters.append("creator", it) }
                    category?.takeIf { it.name.isNotBlank() }?.let { parameters.append("category", it.name) }
                    assignee?.takeIf { it.isNotBlank() }?.let { parameters.append("assignee", it) }
                }
            }
            if (response.status.value in 200..299) ApiResult.Success(response.body())
            else ApiResult.Error(response.call.response.status.toString())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getAllUsers(groupId: Int): ApiResult<List<User>>
    {
        return try
        {
            val response = httpClient.get("/api/user/groupMembers/$groupId")
            if (response.status.value in 200..299) ApiResult.Success(response.body())
            else ApiResult.Error(response.call.response.status.toString())
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Lists every task the caller has completed, across **all** groups they
     * belong to. The server aggregates user-side, so no `X-Group-Id` filter
     * is sent. Pages that need a per-group view filter on `task.groupId`
     * client-side — cheaper than fanning out one request per group, and the
     * server endpoint ignores the header anyway.
     */
    suspend fun getCompleted(): ApiResult<List<Task>>
    {
        return try
        {
            val response = httpClient.get("/api/tasks/completed")
            if (response.status.value in 200..299) ApiResult.Success(response.body())
            else ApiResult.Error(response.call.response.status.toString())
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun checkLogin(): ApiResult<List<Task>> {
        return try {
            val response = httpClient.get("/api/user/me")
            when (response.status.value) {
                in 200..299 -> ApiResult.Success(emptyList())
                401 -> ApiResult.Unauthorized()
                else -> ApiResult.Error(response.status.toString())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }
}
