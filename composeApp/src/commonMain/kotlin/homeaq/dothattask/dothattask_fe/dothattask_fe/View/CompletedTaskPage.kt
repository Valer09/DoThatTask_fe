package homeaq.dothattask.dothattask_fe.dothattask_fe.View


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.TaskDetailDialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.ToastMessage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.TaskCard

@Composable
fun CompletedTaskPage() {

    val taskApi = remember { TaskApi(createHttpClient()) }
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentDetailTask by remember { mutableStateOf<Task?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }


    suspend fun loadTasks() {
        try {
            val result = taskApi.getCompleted()
            if (result is ApiResult.Error)
            {
                toastIsError = true
                toastMessage = result.message
            }
            else if (result is ApiResult.Success) tasks = result.data

        }
        catch (e: Exception)
        {
            errorMessage = "Error loading tasks: ${e.message}"
            println("Error: ${e.message}") // Log per debug
        }
        finally
        {
            isLoading = false
        }
    }

    LaunchedEffect(Unit)
    {
        loadTasks()
    }

    if (currentDetailTask != null) {
        TaskDetailDialog(
            currentDetailTask!!,
            onConfirm = {},
            onDismiss = { currentDetailTask = null }
        )
    }

    Column()
    {
        if(tasks.isEmpty())
        {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 15.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center)
            {
                Text("No tasks completed yet", style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    fontSize = 30.sp)
            }
        }
        if(tasks.isNotEmpty())
        {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 4.dp),
                verticalAlignment = Alignment.Top)
            {
                toastMessage?.let {
                    ToastMessage(
                        message = it,
                        isError = toastIsError,
                        onDismiss = { toastMessage = null }
                    )
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else if (errorMessage != null) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(tasks) { task ->
                            TaskCard(
                                task,
                                onDelete = {},
                                onUpdate = { },
                                onDetails = { currentDetailTask = task },
                                hideDelete = true,
                                hideUpdate = true,
                                onUnassign = {}
                            )
                        }
                    }
                }

            }
        }
    }

}