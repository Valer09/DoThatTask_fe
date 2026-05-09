package homeaq.dothattask.dothattask_fe.dothattask_fe.Model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Tiny global state for badges / counters in the chrome (bottom nav,
 * header). Decoupled from any specific data source on purpose — the UI
 * just observes the values, the page that knows about new data updates
 * them.
 *
 * For now only the invites count is exposed; expand here when other
 * tabs grow notification badges (e.g. unread chat messages).
 */
object NotificationCenter {
    /**
     * Number of pending incoming invites. Consumed by the bottom-nav
     * Invites tab to render the red badge. Setting it to 0 hides the
     * badge entirely. The actual update logic (refresh on poll, on FCM
     * push, on accept/reject) lives in the page that owns the data.
     */
    var pendingInvitesCount: Int by mutableStateOf(0)
}
