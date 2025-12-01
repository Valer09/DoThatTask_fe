package homeaq.dothattask.dothattask_fe.dothattask_fe.Model

import kotlinx.serialization.Serializable

@Serializable
data class TaskUpdate (val oldName: String,
                       val newName: String,
                       val description: String,
                       val category: TaskCategory,
                       val status: TaskStatus,
                       val ownership_username: String)