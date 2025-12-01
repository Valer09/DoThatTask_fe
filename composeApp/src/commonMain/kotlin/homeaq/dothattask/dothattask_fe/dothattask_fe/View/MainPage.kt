package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.User
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.UserListDropdown
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.TaskDetailDialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.ToastMessage


import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.UpdateTaskDialog
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainPage() {


    // Crea httpClient e taskApi solo una volta
    val taskApi = remember { TaskApi(createHttpClient()) }
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var currentTaskToUpdate by remember { mutableStateOf<Task?>(null) }
    var currentDetailTask by remember { mutableStateOf<Task?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }

    var selectedUser by remember { mutableStateOf<User?>(null) }



    suspend fun loadTasks() {
        if (selectedUser == null) return
        try {
            val result = taskApi.getAllTasksLessMine(selectedUser!!.username)
            if (result is ApiResult.Error) {
                toastIsError = true
                toastMessage = result.message
            } else if (result is ApiResult.Success) {
                tasks = result.data
            }
        } catch (e: Exception) {
            errorMessage = "Error loading tasks: ${e.message}"
            println("Error: ${e.message}") // Log per debug
        } finally {
            isLoading = false
        }
    }

    if (currentTaskToUpdate != null) {
        UpdateTaskDialog(
            currentTaskToUpdate!!,
            onConfirm = { scope.launch { loadTasks() } },
            onDismiss = { currentTaskToUpdate = null }
        )
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            UserListDropdown("Select a user", null, {selectedUser = it}, {selectedUser = it})
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { scope.launch { loadTasks() } }, modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)) {
                Text("Get the tasks")
            }
        }

        if(tasks.isNotEmpty())
        {
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)   // Blu "material-ish"
                .padding(horizontal = 2.dp, vertical = 25.dp, ))
            {

                Column(
                    modifier = Modifier
                        .safeContentPadding()
                        .fillMaxSize()
                ) {
                    toastMessage?.let {
                        ToastMessage(
                            message = it,
                            isError = toastIsError,
                            onDismiss = { toastMessage = null }
                        )
                    }

                    // Mostra loading
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    // Mostra errore
                    else if (errorMessage != null) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    // Mostra lista task
                    else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(tasks) { task ->
                                UserListDropdown(
                                    task,
                                    onDelete = {
                                        scope.launch {
                                            when (val result = taskApi.removeTask(it)) {
                                                is ApiResult.Success -> {
                                                    toastIsError = false
                                                    toastMessage = result.message
                                                    loadTasks()
                                                }

                                                is ApiResult.Error -> {
                                                    toastIsError = true
                                                    toastMessage = result.message
                                                }
                                            }
                                        }
                                    },
                                    onUpdate = { currentTaskToUpdate = task },
                                    onDetails = { currentDetailTask = task },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}




