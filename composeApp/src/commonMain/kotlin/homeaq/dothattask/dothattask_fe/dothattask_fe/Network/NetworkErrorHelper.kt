package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException

/**
 * Returns true if [t] is a transport/network-level failure.
 * On Kotlin/JVM these are typed exceptions; on Kotlin/Wasm-JS the browser
 * surfaces a raw JS TypeError ("Fail to fetch" / "Failed to fetch") which
 * does NOT extend Exception — it arrives as a plain Throwable whose message
 * contains a known fetch-failure string.
 */
fun isNetworkError(t: Throwable): Boolean {
    if (t is TimeoutCancellationException) return true
    if (t is SocketTimeoutException) return true
    if (t is HttpRequestTimeoutException) return true
    if (t is ConnectTimeoutException) return true
    if (t is NoTransformationFoundException) return true
    // Kotlin/Wasm-JS: browser fetch failure comes as a Throwable with this message
    val msg = t.message?.lowercase() ?: return false
    return msg.contains("fail to fetch") || msg.contains("failed to fetch") || msg.contains("networkerror")
}

/**
 * Wraps any transport-level failure as an [ApiResult.Error] flagged as
 * isNetwork = true, with a sanitized user-friendly message.
 *
 * Re-throws [CancellationException] (but NOT timeout subclasses) so that
 * normal coroutine cancellation (navigation, scope teardown) is never
 * mistaken for a server outage.
 */
fun networkError(t: Throwable): ApiResult.Error {
    // Let genuine coroutine cancellations propagate — but NOT Ktor timeouts
    // which may wrap CancellationException internally.
    if (t is CancellationException && !isNetworkError(t)) throw t
    return ApiResult.Error(
        message = "Connection error",
        e = if (t is Exception) t else null,
        isNetwork = true,
    )
}
