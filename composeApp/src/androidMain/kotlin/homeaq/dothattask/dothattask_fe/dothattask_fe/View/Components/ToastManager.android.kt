package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual class ToastManager {
    actual fun showToast(
        message: String,
        toastDurationType: ToastDurationType
    ) {
        // Note: This requires context, which is not available here.
        // In a real implementation, use a CompositionLocal or pass context to the manager.
        // For now, this is a stub.
    }
}
