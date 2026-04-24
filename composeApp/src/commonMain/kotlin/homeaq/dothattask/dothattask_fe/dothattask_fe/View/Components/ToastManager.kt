package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

enum class ToastDurationType {
    SHORT,
    LONG
}

expect class ToastManager {
    fun showToast(
        message: String,
        toastDurationType: ToastDurationType = ToastDurationType.SHORT
    )
}
