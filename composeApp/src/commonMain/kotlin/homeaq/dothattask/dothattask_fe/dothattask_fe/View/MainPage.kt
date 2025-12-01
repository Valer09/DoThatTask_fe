package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.TaskCard
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.TaskDetailDialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.ToastMessage
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.UpdateTaskDialog
import kotlinx.coroutines.launch


@Composable
fun MainPage(){
    MaterialTheme {
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


    LaunchedEffect(Unit) {
        try {
            val result = taskApi.getAllTasksDb()
            if(result is ApiResult.Error)
            {
                toastIsError = true
                toastMessage = result.message
            }
            else if(result is ApiResult.Success)
            {
                tasks = result.data
            }
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

    // Dialog per aggiornare task
    if (currentTaskToUpdate != null) {
        UpdateTaskDialog(
            currentTaskToUpdate!!,
            onConfirm = { },
            onDismiss = { currentTaskToUpdate = null   }
        )
    }

    if (currentDetailTask != null) {
        TaskDetailDialog(
            currentDetailTask!!,
            onConfirm = {},
            onDismiss = { currentDetailTask = null }
        )
    }

    // UI
    Column(
        modifier = Modifier
            .safeContentPadding()
            .fillMaxSize()
    ) {
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
                    TaskCard(
                        task,
                        onDelete = {
                            scope.launch {
                                when (val result = taskApi.removeTask(it)) {
                                    is ApiResult.Success -> {
                                        toastIsError = false
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

    toastMessage?.let {
        ToastMessage(
            message = it,
            isError = toastIsError,
            onDismiss = { toastMessage = null }
        )
    }
    }
}




