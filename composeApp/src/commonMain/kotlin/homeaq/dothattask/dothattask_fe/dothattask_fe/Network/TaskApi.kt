package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
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
            val response = httpClient.delete("tasks/${task.name}")

            if (response.status.value in 200..299) ApiResult.Success("Task deleted successfully")
            else return ApiResult.Error(response.call.response.status.toString())
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
            val response = httpClient.post("/tasks")
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

    suspend fun getAllTasksDb(): ApiResult<List<Task>> {
        return try
        {
            val response = httpClient.get("/tasks")
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
            val response = httpClient.get("/user/usersLessMe")
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
            val response = httpClient.get("/tasks/tasksByUser/$username")
            if (response.status.value in 200..299) ApiResult.Success(response.body())
            else ApiResult.Error(response.call.response.status.toString())
        }
        catch (e: Exception)
        {
            return ApiResult.Error(e.message ?: "Unknown error")
        }
    }

}