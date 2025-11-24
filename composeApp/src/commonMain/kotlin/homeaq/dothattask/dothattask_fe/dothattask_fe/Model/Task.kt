package homeaq.dothattask.dothattask_fe.dothattask_fe.Model

import kotlinx.serialization.Serializable

enum class TaskStatus {
    TODO,
    ACTIVE,
    COMPLETED,
}

@Serializable
data class Task
    (
        val name: String,
        val description: String,
        val status: TaskStatus
    )
