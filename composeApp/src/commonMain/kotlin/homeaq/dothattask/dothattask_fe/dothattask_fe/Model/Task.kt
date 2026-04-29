package homeaq.dothattask.dothattask_fe.dothattask_fe.Model

import kotlinx.serialization.Serializable

enum class TaskStatus(val code: Int) {
    TODO(1),
    ACTIVE(2),
    COMPLETED(3)
}

/**
 * Was an enum (Social/Career/Health). Now a data class because users can
 * register custom categories per group on the backend. Default ids are
 * stable: 1=Social, 2=Career, 3=Health — they keep working as legacy
 * fallbacks when a Task arrives with an unknown id.
 */
@Serializable
data class TaskCategory(
    val id: Int = 0,
    val name: String = "",
    val color: String = "",
) {
    companion object {
        // Legacy defaults — used as fallback or when the dynamic list is
        // unavailable. The colors mirror the backend's pre-seed defaults.
        val Social = TaskCategory(1, "Social", "#42A5F5")
        val Career = TaskCategory(2, "Career", "#26A69A")
        val Health = TaskCategory(3, "Health", "#EF5350")
        val Defaults = listOf(Social, Career, Health)

        /** Same normalization the backend applies. */
        fun normalizeName(raw: String): String {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return trimmed
            return trimmed[0].uppercaseChar() + trimmed.substring(1).lowercase()
        }
    }
}

@Serializable
data class Task
    (
    val name: String,
    val description: String,
    val category: TaskCategory,
    val status: TaskStatus,
    val ownership_username: String,
    val groupId: Int = 0,
    val groupName: String = "",
    val groupColor: String = "",
    val createdAt: String = "",
)
