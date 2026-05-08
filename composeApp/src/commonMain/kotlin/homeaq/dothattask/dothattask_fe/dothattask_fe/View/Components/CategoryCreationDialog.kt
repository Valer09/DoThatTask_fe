package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.client
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.ApiResult
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.CategoryApi
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.routeIfNetwork
import homeaq.dothattask.dothattask_fe.dothattask_fe.View.TaskUIHelper
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCreationDialog(groupId: Int, onConfirm: suspend () -> Unit, onClose: () -> Unit)
{
    val api = remember { CategoryApi(client()) }
    var newName by remember(groupId) { mutableStateOf("") }
    var newColor by remember(groupId) { mutableStateOf("") }
    var error by remember(groupId) { mutableStateOf<String?>(null) }
    var nameValidationError by remember(groupId) { mutableStateOf<String?>(null) }
    var colorValidationError by remember(groupId) { mutableStateOf<String?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastIsError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val colors = TaskUIHelper.appTextFieldColors()
    var loading by remember { mutableStateOf(false) }
    var submitAttempted by remember { mutableStateOf(false) }
    var nameTouched by remember { mutableStateOf(false) }
    var colorTouched by remember { mutableStateOf(false) }
    val trimmedName = newName.trim()
    val trimmedColor = newColor.trim()
    val hexColorRegex = remember { Regex("^#?([A-Fa-f0-9]{6})$") }
    val isNameInvalid = trimmedName.isEmpty()
    val isColorInvalid = trimmedColor.isNotBlank() && !hexColorRegex.matches(trimmedColor)
    val showNameError = isNameInvalid && (nameTouched || submitAttempted)
    val showColorError = isColorInvalid && (colorTouched || submitAttempted)


    Box(modifier = Modifier
        .fillMaxSize().wrapContentSize(Alignment.Center)){

        LoadingOverlay(isLoading = loading, Color.Transparent)

        Dialog(onDismissRequest = {}) {
            Column()
            {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(5.dp),
                    shape = TaskUIHelper.appCardShape(),
                    colors = TaskUIHelper.appCardColors(),
                ) {
                    toastMessage?.let {
                        ToastMessage(
                            message = it,
                            isError = toastIsError,
                            onDismiss = { toastMessage = null; toastIsError = false }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().background(TaskUIHelper.getPrimary()).padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    )
                    {
                        Text("Create new category", fontSize = 20.sp, color = Color.White)
                    }


                    Column(modifier = Modifier.padding(10.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        )
                        {
                            TextField(
                                value = newName,
                                onValueChange = {
                                    newName = it
                                    nameTouched = true
                                    error = null
                                },
                                isError = showNameError,
                                label = { Text("Name") },
                                colors = colors,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            )
                        }

                        if (showNameError) {
                            nameValidationError?.let {
                                Text(
                                    text = it,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        } else {
                            Spacer(Modifier.height(8.dp))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        )
                        {
                            TextField(
                                value = newColor,
                                onValueChange = {
                                    newColor = it
                                    colorTouched = true
                                    error = null
                                },
                                isError = showColorError,
                                label = { Text("Color (#RRGGBB)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                colors = TaskUIHelper.appTextFieldColors(),
                            )
                        }

                        if (showColorError) {
                            colorValidationError?.let {
                                Text(
                                    text = it,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        } else {
                            Spacer(Modifier.height(8.dp))
                        }


                        Row(modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically)
                        {
                            error?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(it, color = Color.Red, fontSize = 12.sp)
                            }
                        }

                        Spacer(Modifier.height(30.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        )
                        {
                            OutlinedButton(
                                modifier = Modifier.appButtonSizeSmall().pointerHoverIcon(PointerIcon.Hand, true),
                                onClick = { onClose() },
                            )
                            {
                                Text("Close")
                            }
                            OutlinedButton(
                                onClick = {
                                    submitAttempted = true
                                    nameTouched = true
                                    colorTouched = true

                                    if (isNameInvalid) {
                                        nameValidationError = "Category name cannot be empty"
                                        return@OutlinedButton
                                    }

                                    if (isColorInvalid) {
                                        colorValidationError = "Color must be a valid hex code like #A1B2C3"
                                        return@OutlinedButton
                                    }
                                    val name = newName.trim()
                                    val trimmedColor = newColor.trim()
                                    scope.launch {
                                        loading = true
                                        when (val res = api.create(groupId, name, trimmedColor.takeIf { it.isNotBlank() })) {
                                            is ApiResult.Success -> {
                                                newName = ""
                                                newColor = ""
                                                error = null
                                                toastMessage = "category $name created"
                                                onConfirm()
                                                submitAttempted = false
                                                nameTouched = false
                                                colorTouched = false
                                            }
                                            is ApiResult.Error -> if (!res.routeIfNetwork())
                                            {
                                                toastMessage = res.message
                                                toastIsError = true
                                            }
                                            is ApiResult.NotFound ->
                                                {
                                                    toastMessage = res.message
                                                    toastIsError = true
                                                }
                                            is ApiResult.Unauthorized -> {
                                                toastMessage = "Unauthorized"
                                                AppState.currentScreen = Screen.Login
                                            }
                                            is ApiResult.Forbidden -> {
                                                toastMessage = "Forbidden"
                                                toastIsError = true
                                            }
                                        }
                                        loading = false
                                    }
                                },
                                enabled = !loading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TaskUIHelper.getComplementary(),
                                    contentColor = Color.Black,
                                ),
                                modifier = Modifier.appButtonSizeSmall().pointerHoverIcon(PointerIcon.Hand, true),
                            ) { Text("Add") }
                        }
                    }
                }
            }
        }
    }
}
