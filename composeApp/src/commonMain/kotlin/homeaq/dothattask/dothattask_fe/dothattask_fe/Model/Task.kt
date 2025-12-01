package homeaq.dothattask.dothattask_fe.dothattask_fe.Model

import kotlinx.serialization.Serializable

enum class TaskStatus {
    TODO,
    ACTIVE,
    COMPLETED,
}

enum class TaskCategory(val code: Int)
{
    Social(1),
    Career(2),
    Health(3);

    companion object {
        fun fromCode(code: Int) = entries.first { it.code == code }
    }
}

@Serializable
data class Task
    (
    val name: String,
    val description: String,
    val category: TaskCategory,
    val status: TaskStatus,
    val ownership_username: String
)
