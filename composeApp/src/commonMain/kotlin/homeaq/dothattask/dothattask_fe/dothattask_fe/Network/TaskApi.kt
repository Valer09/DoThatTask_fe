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
            val response = httpClient.delete("api/tasks/${task.name}")

            if (response.status.value in 200..299) ApiResult.Success("Task deleted successfully")
            else return ApiResult.Error(response.call.response.status.toString())
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }
    suspend fun createTask(task: Task) : ApiResult<Task>
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
                contentType(ContentType.Application.Json)
                url{
                    parameters.append("username",   task.ownership_username)
                    setBody(taskUpdate)
                }

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

    suspend fun getAssignedTask() :  ApiResult<Task>
    {
        return try
        {
            val response = httpClient.get("/api/tasks/assignedTask")

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

    suspend fun pickTask(category: TaskCategory):  ApiResult<Task>
    {
        return try
        {
            val response = httpClient.post("/api/tasks/pickTask")
            {
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
                url{
                    parameters.append("task_name", task.name)
                }
                contentType(ContentType.Application.Json)
            }

            when (response.status.value) {
                in 200..299 -> ApiResult.Success(task)
                404 -> ApiResult.NotFound(response.body())
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

    suspend fun getAllTasksDb(): ApiResult<List<Task>> {
        return try
        {
            val response = httpClient.get("/api/tasks")
            if (response.status.value in 200..299) ApiResult.Success(response.body())
            else ApiResult.Error(response.call.response.status.toString())
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getAllUsers(): ApiResult<List<User>>
    {
        return try
        {
            val response = httpClient.get("/api/user/usersLessMe")
            if (response.status.value in 200..299) ApiResult.Success(response.body())
            else ApiResult.Error(response.call.response.status.toString())
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }

   suspend fun getAllTasksLessMine(username: String) : ApiResult<List<Task>>
    {
        return try
        {
            val response = httpClient.get("/api/tasks/tasksByUser/$username")
            if (response.status.value in 200..299) ApiResult.Success(response.body())
            else ApiResult.Error(response.call.response.status.toString())
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getCompleted() :  ApiResult<List<Task>>
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

    suspend fun checkLogin() :  ApiResult<List<Task>>
    {
        return try
        {
            val response = httpClient.get("/api/user/me")
            if (response.status.value in 200..299) ApiResult.Success(emptyList())
            else ApiResult.Error(response.call.response.status.toString())
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }
}