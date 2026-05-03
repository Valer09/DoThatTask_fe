package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T, val message: String = "Completed!") : ApiResult<T>()

    /**
     * [isNetwork] distinguishes a transport-level failure (no connection,
     * timeout, DNS, server unreachable — i.e. an exception thrown by the
     * Ktor client and wrapped in the API's `catch` block) from a server
     * response we successfully parsed as a non-success status (validation
     * errors, conflicts, etc.). Only the former should bubble up to the
     * full-screen error page; the latter are usually actionable inline.
     *
     * Use [routeIfNetwork] from a call site to fork the two paths in one
     * line.
     */
    data class Error(val message: String, val isNetwork: Boolean = false) : ApiResult<Nothing>()
    data class NotFound(val message: String) : ApiResult<Nothing>()
    data class Unauthorized(val message: String = "Unauthorized") : ApiResult<Nothing>()
}

/**
 * If this is a transport-level failure, switch to the global error page and
 * return `true`; otherwise return `false` so the caller can fall through to
 * its inline error UI.
 */
fun ApiResult.Error.routeIfNetwork(): Boolean {
    if (!isNetwork) return false
    AppState.routeToError(message)
    return true
}
