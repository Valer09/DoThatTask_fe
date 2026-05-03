package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import kotlinx.coroutines.CancellationException

/**
 * Wraps a transport-level failure as an [ApiResult.Error] flagged as a
 * network error. Re-throws [CancellationException] so coroutine cancellation
 * (page navigation, scope teardown) is never mistaken for a server outage and
 * doesn't kick the user to the global ErrorPage.
 */
fun networkError(e: Exception): ApiResult.Error {
    if (e is CancellationException) throw e
    return ApiResult.Error(e.message ?: "Unknown error", isNetwork = true)
}