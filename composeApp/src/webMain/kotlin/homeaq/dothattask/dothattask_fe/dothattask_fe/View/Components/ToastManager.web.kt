package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

actual class ToastManager {
    actual fun showToast(
        message: String,
        toastDurationType: ToastDurationType
    ) {
        // Web (JS) stub: Could use window.alert or a custom toast library.
        println("[Toast] $message")
    }
}
