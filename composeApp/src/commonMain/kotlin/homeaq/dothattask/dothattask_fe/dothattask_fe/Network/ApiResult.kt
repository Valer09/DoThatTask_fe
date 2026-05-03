package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T, val message: String = "Completed!") : ApiResult<T>()
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
