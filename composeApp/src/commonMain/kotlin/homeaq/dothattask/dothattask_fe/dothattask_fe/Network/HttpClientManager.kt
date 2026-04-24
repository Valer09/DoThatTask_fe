package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import homeaq.dothattask.dothattask_fe.dothattask_fe.Config.Environment
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.auth.AuthTokens
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.auth.RefreshRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
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

// Client used for auth endpoints (login/register/refresh) - no Authorization
// header, never intercepted by the Auth plugin. Also used internally by the
// main client's refresh handler.
fun createUnauthenticatedClient() = HttpClient {
    install(ContentNegotiation) { json(json) }
    defaultRequest {
        url {
            protocol = envProtocol
            host = Environment.HOST
            port = Environment.PORT
        }
    }
}

// The primary client used by authenticated API calls. Uses the Ktor Auth
// bearer plugin, which transparently calls the refresh endpoint on 401 and
// retries the original request with the new access token. If the refresh
// itself fails (refresh token missing/revoked/expired), onRefreshFailed is
// invoked so the UI can route the user back to Login.
fun createHttpClient(onRefreshFailed: () -> Unit = {}) = HttpClient {
    install(ContentNegotiation) { json(json) }
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
                    AuthState.groupId = tokens.user.groupId
                    AuthState.persist()
                    BearerTokens(tokens.accessToken, tokens.refreshToken)
                } catch (_: Exception) {
                    AuthState.clear()
                    onRefreshFailed()
                    null
                }
            }
            // Send the access token proactively on every call except the
            // auth endpoints (login/register/refresh). The plugin itself
            // handles refresh reactively when needed.
            sendWithoutRequest { request ->
                val url = request.url.buildString()
                !url.contains("/api/auth/login") &&
                    !url.contains("/api/auth/register") &&
                    !url.contains("/api/auth/refresh")
            }
        }
    }
    defaultRequest {
        AuthState.token?.let { token -> header("Authorization", "Basic $token") }
        url {
            protocol = envProtocol
            host = Environment.HOST
            port = Environment.PORT
        }

    }

}

// Legacy overload kept so call sites that pass a Basic token still compile
// while we migrate screens. Remove once LoginPage no longer calls it.
@Deprecated("Use createHttpClient() with AuthState instead", ReplaceWith("createHttpClient()"))
fun createHttpClient(token: String?) = HttpClient {
    install(ContentNegotiation) { json(json) }
    defaultRequest {
        token?.let { header("Authorization", "Basic $it") }
        url {
            protocol = envProtocol
            host = Environment.HOST
            port = Environment.PORT
        }
    }
}
