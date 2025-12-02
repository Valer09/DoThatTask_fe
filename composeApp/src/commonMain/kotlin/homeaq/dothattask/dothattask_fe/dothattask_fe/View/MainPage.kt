package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.User
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.UserListDropdown
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.TaskDetailDialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.ToastMessage


import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.UpdateTaskDialog
import io.ktor.client.call.body
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainPage(
) {
    val taskApi = remember { TaskApi(createHttpClient()) }
    var assignedTask by remember { mutableStateOf<Task?>(null) }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun loadAssignedTask() {
        try {
            val result = taskApi.getAssignedTask()

            if (result is ApiResult.Success) assignedTask = result.data
            else if (result is ApiResult.NotFound) assignedTask = null
            else if (result is ApiResult.Error) {
                toastIsError = true
                toastMessage = result.message
            }
        } catch (e: Exception) {
            toastIsError = true
            toastMessage = "Failed to load users: ${e.message}"
        }
    }

    suspend fun pickTask(): Unit  {
        try {
            val result = taskApi.pickTask()

            if (result is ApiResult.Success) loadAssignedTask()
            else if (result is ApiResult.NotFound)
            {
                toastIsError = false
                toastMessage = "No assignable task for this user. Please assign tasks to the user"
                loadAssignedTask()
            }
            else if (result is ApiResult.Error)
            {
                toastIsError = true
                toastMessage = result.message
            }
        } catch (e: Exception) {
            toastIsError = true
            toastMessage = "Failed to pick a task: ${e.message}"
        }
    }

    suspend fun complete(): Unit  {
        try {
            val result = taskApi.completeTask(assignedTask)

            if (result is ApiResult.Success) loadAssignedTask()
            else if (result is ApiResult.Error)
            {
                toastIsError = true
                toastMessage = result.message
            }
        } catch (e: Exception) {
            toastIsError = true
            toastMessage = "Failed to complete the task: ${e.message}"
        }
    }

    LaunchedEffect(Unit)
    {
        loadAssignedTask()
    }

    Column{
        toastMessage?.let {
            Row (modifier = Modifier
                .fillMaxWidth(),
                verticalAlignment = Alignment.Top)
            {
                ToastMessage(
                    message = it,
                    isError = toastIsError,
                    onDismiss = { toastMessage = null })
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        )
        {
            Column {
                Text(
                    text = "Task to accomplish this week",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        if (assignedTask != null)
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .fillMaxHeight()
                    .background(TaskUIHelper.getLightGray()),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween)
            {
                Column(modifier = Modifier.weight(1f)
                    .padding(20.dp), horizontalAlignment = Alignment.Start){
                    Row()
                    {
                        Column {  Text(
                            text = "Name: ",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )}
                        Column {  Text(
                            text = "${assignedTask?.name}",
                            fontWeight = FontWeight.Normal,
                            style = MaterialTheme.typography.bodyLarge
                        )}
                    }

                    Row()
                    {
                        Column {  Text(
                            text = "Category: ",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )}
                        Column {  Text(
                            text = "${assignedTask?.name}",
                            fontWeight = FontWeight.Normal,
                            style = MaterialTheme.typography.bodyLarge
                        )}
                    }

                    Row()
                    {
                        Column {  Text(
                            text = "Description: ",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )}
                        Column {  Text(
                            text = assignedTask?.description ?: "N/A",
                            fontWeight = FontWeight.Normal,
                            style = MaterialTheme.typography.bodyLarge
                        )}
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f, )
                    .padding(20.dp), horizontalAlignment = Alignment.End)
                {
                    Button(
                        onClick = {
                            scope.launch { complete() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TaskUIHelper.getGreen())
                    ) {
                        Text("Complete", color = Color.White)
                    }
                }
            }
        }
        else
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .fillMaxHeight()
                    .background(TaskUIHelper.getLightGray()),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween)
            {
                Column(modifier = Modifier.weight(1f)
                    .padding(20.dp), horizontalAlignment = Alignment.Start)
                {
                    Text(
                        text = "No task assigned",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f, )
                    .padding(20.dp), horizontalAlignment = Alignment.End)
                {
                    Button(
                        onClick = {
                            scope.launch { pickTask() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TaskUIHelper.getMarinerBlue())
                    ) {
                        Text("Pick a task", color = Color.White)
                    }
                }


            }
        }
    }



}




