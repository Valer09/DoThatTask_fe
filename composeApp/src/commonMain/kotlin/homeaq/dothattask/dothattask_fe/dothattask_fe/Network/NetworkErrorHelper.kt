package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException

/**
 * Wraps a transport-level failure as an [ApiResult.Error] flagged as a
 * network error. Re-throws [CancellationException] so coroutine cancellation
 * (page navigation, scope teardown) is never mistaken for a server outage and
 * doesn't kick the user to the global ErrorPage.
 */
fun networkError(e: Exception): ApiResult.Error {
    if (checkExceptionType(e))
        return ApiResult.Error(e.message ?: "Timeout", e, isNetwork = true)

    if (e is CancellationException) throw e
    return ApiResult.Error(e.message ?: "Unknown error", e, isNetwork = true)
}

fun checkExceptionType(e: Exception?): Boolean {
    return e is TimeoutCancellationException
            || e is SocketTimeoutException
            || e is HttpRequestTimeoutException
            || e is NoTransformationFoundException
            || e is ConnectTimeoutException

}