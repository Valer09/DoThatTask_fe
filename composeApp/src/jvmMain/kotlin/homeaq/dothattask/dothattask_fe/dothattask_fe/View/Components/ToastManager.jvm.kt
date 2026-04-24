package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

actual class ToastManager {
    actual fun showToast(
        message: String,
        toastDurationType: ToastDurationType
    ) {
        // Desktop (JVM) stub: Could integrate with system notifications or just print.
        println("[Toast] $message")
    }
}
