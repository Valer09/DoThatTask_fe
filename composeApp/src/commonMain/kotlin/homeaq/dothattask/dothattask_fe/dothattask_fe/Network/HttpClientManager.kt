package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import homeaq.dothattask.dothattask_fe.dothattask_fe.Config.Environment
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.auth.AuthTokens
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.auth.RefreshRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private val envProtocol: URLProtocol
    get() = if (Environment.SCHEME == "https") URLProtocol.HTTPS else URLProtocol.HTTP

private val json = Json {
    encodeDefaults = true
    isLenient = true
    coerceInputValues = true
    ignoreUnknownKeys = true
}

// Bound HTTP timeouts so a server that never responds eventually surfaces an
// exception (caught by the API layer → routed to ErrorPage) instead of the
// caller's spinner hanging forever.
private const val REQUEST_TIMEOUT_MILLIS: Long = 15_000
private const val CONNECT_TIMEOUT_MILLIS: Long = 10_000
private const val SOCKET_TIMEOUT_MILLIS: Long = 15_000

// Client used for auth endpoints (login/register/refresh) — no Authorization
// header, never intercepted by the Auth plugin.
fun createUnauthenticatedClient() = HttpClient {
    install(ContentNegotiation) { json(json) }
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
        socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
    }
    defaultRequest {
        url {
            protocol = envProtocol
            host = Environment.HOST
            port = Environment.PORT
        }
    }
}

/**
 * The primary client used by authenticated API calls. Uses the Ktor Auth
 * bearer plugin, which transparently calls the refresh endpoint on 401 and
 * retries the original request with the new access token. If the refresh
 * itself fails (refresh token missing/revoked/expired), [onRefreshFailed] is
 * invoked so the UI can route the user back to Login.
 *
 * Multi-group: each group-scoped call attaches its own `X-Group-Id` via
 * [withGroup]. We deliberately don't set it from a default request — having
 * two callers (one default, one explicit) would otherwise emit two headers
 * and the server would reject the concatenated value as not parsable.
 */
fun createHttpClient(onRefreshFailed: () -> Unit = {}) = HttpClient {
    install(ContentNegotiation) { json(json) }
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
        socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
    }
    install(Auth) {
        bearer {
            loadTokens {
                val access = AuthState.accessToken
                val refresh = AuthState.refreshToken
                if (access != null && refresh != null) BearerTokens(access, refresh) else null
            }
            refreshTokens {
                val refresh = AuthState.refreshToken ?: run {
                    onRefreshFailed()
                    return@refreshTokens null
                }
                try {
                    val tokens: AuthTokens = client.post("/api/auth/refresh") {
                        markAsRefreshTokenRequest()
                        contentType(ContentType.Application.Json)
                        setBody(RefreshRequest(refresh))
                    }.body()
                    AuthState.accessToken = tokens.accessToken
                    AuthState.refreshToken = tokens.refreshToken
                    AuthState.groups = tokens.user.groups
                    val current = AuthState.activeGroupId
                    AuthState.activeGroupId =
                        if (current != null && tokens.user.groups.any { it.id == current }) current
                        else tokens.user.groups.firstOrNull()?.id
                    AuthState.persist()
                    BearerTokens(tokens.accessToken, tokens.refreshToken)
                } catch (_: ResponseException) {
                    AuthState.clear()
                    onRefreshFailed()
                    null
                }catch (_: Exception) {
                    // Network/transport error (no connectivity, timeout, DNS, …).
                    null
                }
            }
            sendWithoutRequest { request ->
                val url = request.url.buildString()
                !url.contains("/api/auth/login") &&
                    !url.contains("/api/auth/register") &&
                    !url.contains("/api/auth/refresh")
            }
        }
    }
    defaultRequest {
        url {
            protocol = envProtocol
            host = Environment.HOST
            port = Environment.PORT
        }
    }
}
