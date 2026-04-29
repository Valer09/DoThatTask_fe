package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Task
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.TaskCategory
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.CategoryApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.TaskApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.createHttpClient
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.GroupBadge
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.LoadingOverlay
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components.ToastMessage


import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
) {
    val taskApi = remember { TaskApi(createHttpClient()) }
    val categoryApi = remember { CategoryApi(createHttpClient()) }
    var assignedTask by remember { mutableStateOf<Task?>(null) }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }

    // The dropdown is fed from the active group's category list. Defaults
    // act as a placeholder while the network fetch is in flight or when no
    // group is active yet.
    var availableCategories by remember { mutableStateOf(TaskCategory.Defaults) }
    var category by remember { mutableStateOf(TaskCategory.Social) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val activeGroupId = AuthState.activeGroupId
    LaunchedEffect(activeGroupId) {
        val gid = activeGroupId ?: return@LaunchedEffect
        when (val result = categoryApi.list(gid)) {
            is ApiResult.Success -> {
                if (result.data.isNotEmpty()) {
                    availableCategories = result.data
                    if (availableCategories.none { it.id == category.id }) {
                        category = availableCategories.first()
                    }
                }
            }
            else -> {}
        }
    }

    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }

    suspend fun loadAssignedTask() {
        loading =  true
        try {
            val result = taskApi.getAssignedTask()

            if (result is ApiResult.Success) assignedTask = result.data
            else if (result is ApiResult.NotFound) assignedTask = null
            else if (result is ApiResult.Error) {
                toastIsError = true
                toastMessage = result.message
            }
        }
        catch (e: Exception)
        {
            toastIsError = true
            toastMessage = "Failed to load users: ${e.message}"
        }finally
        {
            loading =  false
        }
    }

    suspend fun pickTask(category: TaskCategory): Unit  {
        loading =  true
        try {
            val result = taskApi.pickTask(category)

            if (result is ApiResult.Success) loadAssignedTask()
            else if (result is ApiResult.NotFound)
            {
                toastIsError = false
                toastMessage = "No assignable task in this category for this user. Please assign tasks to the user"
                loadAssignedTask()
            }
            else if (result is ApiResult.Error)
            {
                toastIsError = true
                toastMessage = result.message
            }
        }
        catch (e: Exception)
        {
            toastIsError = true
            toastMessage = "Failed to pick a task: ${e.message}"
        }
        finally
        {
            loading =  false
        }
    }

    suspend fun complete(): Unit  {
        loading =  true
        try {
            val result = taskApi.completeTask(assignedTask)

            if (result is ApiResult.Success) loadAssignedTask()
            else if (result is ApiResult.Error)
            {
                toastIsError = true
                toastMessage = result.message
            }
        }
        catch (e: Exception)
        {
            toastIsError = true
            toastMessage = "Failed to complete the task: ${e.message}"
        }
        finally
        {
            loading =  false
        }
    }

    LaunchedEffect(Unit)
    {
        loadAssignedTask()
    }
Box{
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
            horizontalArrangement = Arrangement.Center
        )
        {
            Column {
                Text(
                    text = "Task to accomplish this week",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
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
                    .weight(0.70f)
                    .background(TaskUIHelper.getLightGray()),
                verticalAlignment = Alignment.Top)
            {
                Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally)
                {
                    Row(modifier = Modifier.weight(0.2f).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically)
                    {
                        Text(
                            text = "${assignedTask?.name}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 23.sp,
                        )
                        assignedTask?.let { t ->
                            if (t.groupName.isNotBlank()) {
                                Spacer(modifier = Modifier.width(10.dp))
                                GroupBadge(t.groupName, t.groupColor)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    Row(modifier = Modifier.weight(0.8f).fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.Top)
                    {
                        Column()
                        {
                            Row()
                            {
                                Column(modifier = Modifier.weight(0.3f).padding(vertical = 8.dp)) {
                                    Text(
                                        text = "Category: ",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 17.sp,
                                    )}

                                Column(modifier = Modifier.weight(0.7f).padding(vertical = 8.dp).padding(horizontal = 4.dp)) {
                                    assignedTask?.category?.let {
                                        Text(
                                            text = "${assignedTask?.category}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontSize = 17.sp,
                                            color = TaskUIHelper.pickColor(it)
                                        )
                                    }
                                }
                            }
                            Row()
                            {
                                Column(modifier = Modifier.weight(0.3f).padding(vertical = 8.dp)) {
                                    Text(
                                        text = "Description: ",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 17.sp,
                                    )}

                                Column(modifier = Modifier.weight(0.7f).padding(vertical = 8.dp).padding(horizontal = 4.dp)) {
                                    Text(
                                        text = assignedTask?.description ?: "N/A",
                                        fontWeight = FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 17.sp,
                                    )}
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
        else
        {
            Row(modifier = Modifier.weight(0.35f).background(TaskUIHelper.getLightGray()).padding(20.dp))
            {
                Column(modifier = Modifier.height(300.dp))
                {
                    Row(modifier = Modifier.fillMaxWidth().weight(0.3f), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.Center)
                    {

                        Text(
                            text = "No task assigned. Pick a task",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            fontSize = 25.sp
                        )

                    }
                    Spacer(modifier = Modifier.height(25.dp))
                    Row(modifier = Modifier.fillMaxWidth().weight(0.4f))
                    {
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                            modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true)

                        ) {
                            TextField(

                                value = category.name,
                                colors = TextFieldDefaults.colors(focusedTextColor = TaskUIHelper.pickColor(category), unfocusedTextColor = TaskUIHelper.Companion.pickColor(category)),
                                onValueChange = {},
                                label = { Text("Category", fontSize = 18.sp) },
                                textStyle = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                    .pointerHoverIcon(PointerIcon.Hand, true).height(60.dp)

                            )

                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false },
                                modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true),
                            ) {
                                availableCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name, fontWeight = FontWeight.Bold, color = TaskUIHelper.pickColor(cat)) },
                                        onClick = {
                                            category = cat
                                            categoryExpanded = false
                                        },
                                        modifier =     Modifier.pointerHoverIcon(PointerIcon.Hand, true).height(60.dp) ,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(25.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.3f),
                        verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center
                    )
                    {
                        Button(
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true).padding(horizontal = 16.dp),
                            onClick = {
                                scope.launch { pickTask(category) }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TaskUIHelper.getMarinerBlue()),

                            ) {
                            Text("Pick a new task!", color = Color.White,
                                modifier = Modifier.padding(horizontal = 20.dp).padding(vertical = 10.dp),
                                fontSize = 20.sp)
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .weight(0.35f)
                .background(Color.White),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center)
        {

            Button(
                modifier = Modifier
                    .pointerHoverIcon(PointerIcon.Hand, true)
                    .weight(1.2f)
                    .padding(horizontal = 5.dp)
                    .padding(vertical = 8.dp),
                onClick = {
                    scope.launch { AppState.currentScreen =  Screen.CompletedTask }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TaskUIHelper.getMarinerBlue()))
            {
                Text("Completed tasks", color = Color.White, fontSize = 17.sp, modifier = Modifier.padding(vertical = 10.dp))
            }
            if (assignedTask != null)
            {
                Button(
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand, true)
                        .weight(1.2f)
                        .padding(horizontal = 5.dp)
                        .padding(vertical = 8.dp),
                    onClick = {
                        scope.launch { complete() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TaskUIHelper.getGreen())
                ) {
                    Text("Complete task!", color = Color.Black, fontSize = 17.sp, modifier = Modifier.padding(vertical = 10.dp))
                }
            }
        }
    }

    LoadingOverlay(isLoading = loading)

    }

}




