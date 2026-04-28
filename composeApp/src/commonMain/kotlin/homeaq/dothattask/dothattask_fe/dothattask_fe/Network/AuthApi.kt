package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.auth.AuthTokens
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.auth.ChangePasswordRequest
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.auth.LoginRequest
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.auth.LogoutRequest
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.auth.RefreshRequest
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.auth.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * [unauthenticated] is used for login/register/logout where we may
 * not yet have a valid bearer token.
 * [authenticated] is used for change-password which requires a bearer.
 */
class AuthApi(
    private val unauthenticated: HttpClient,
    private val authenticated: HttpClient,
) {
    suspend fun login(username: String, password: String): ApiResult<AuthTokens> = try {
        val resp = unauthenticated.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }
        when (resp.status.value) {
            in 200..299 -> {
                val tokens: AuthTokens = resp.body()
                applyTokens(tokens)
                ApiResult.Success(tokens)
            }
            401 -> ApiResult.Error("Invalid credentials")
            405 -> ApiResult.Error("Method not allowed")
            else -> ApiResult.Error("Login failed (${resp.status.value})")
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }

    suspend fun register(name: String, username: String, password: String): ApiResult<AuthTokens> = try {
        val resp = unauthenticated.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(name, username, password))
        }
        when (resp.status.value) {
            in 200..299 -> {
                val tokens: AuthTokens = resp.body()
                applyTokens(tokens)
                ApiResult.Success(tokens)
            }
            409 -> ApiResult.Error("Username already taken")
            else -> ApiResult.Error("Registration failed (${resp.status.value})")
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }

    suspend fun logout(): ApiResult<String> = try {
        val refresh = AuthState.refreshToken
        if (refresh != null) {
            unauthenticated.post("/api/auth/logout") {
                contentType(ContentType.Application.Json)
                setBody(LogoutRequest(refresh))
                // Logout requires the bearer on the backend; attach it manually
                // since we're using the unauthenticated client here.
                AuthState.accessToken?.let { token ->
                    headers.append("Authorization", "Bearer $token")
                }
            }
        }
        AuthState.clear()
        ApiResult.Success("Logged out")
    } catch (e: Exception) {
        // Even if the server is unreachable, drop local state so the user
        // sees a "logged out" UI.
        AuthState.clear()
        ApiResult.Error(e.message ?: "Network error")
    }

    /**
     * Force a refresh of the access token. Useful after a state change on the
     * server (e.g. a new group joined/created) so [AuthState.groups] is
     * reloaded without waiting for natural access-token expiry.
     */
    suspend fun refresh(): ApiResult<AuthTokens> = try {
        val refresh = AuthState.refreshToken
            ?: return ApiResult.Error("No refresh token present")
        val resp = unauthenticated.post("/api/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refresh))
        }
        when (resp.status.value) {
            in 200..299 -> {
                val tokens: AuthTokens = resp.body()
                applyTokens(tokens)
                ApiResult.Success(tokens)
            }
            401 -> {
                AuthState.clear()
                ApiResult.Error("Session expired")
            }
            else -> ApiResult.Error("Refresh failed (${resp.status.value})")
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): ApiResult<String> = try {
        val resp = authenticated.post("/api/auth/change-password") {
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequest(oldPassword, newPassword))
        }
        when (resp.status.value) {
            in 200..299 -> ApiResult.Success("Password changed")
            403 -> ApiResult.Error("Old password is incorrect")
            else -> ApiResult.Error("Change password failed (${resp.status.value})")
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Network error")
    }

    private fun applyTokens(tokens: AuthTokens) {
        AuthState.setSession(
            username = tokens.user.username,
            displayName = tokens.user.name,
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            groups = tokens.user.groups,
        )
        AuthState.persist()
    }
}
