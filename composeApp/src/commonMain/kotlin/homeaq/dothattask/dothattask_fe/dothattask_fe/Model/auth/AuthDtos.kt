package homeaq.dothattask.dothattask_fe.dothattask_fe.Model.auth

import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupSummary
import kotlinx.serialization.Serializable

/**
 * Login is now email-based. The previous username field is removed; the
 * BE accepts email values and falls back to legacy username matching for
 * accounts that pre-date the email migration.
 */
@Serializable
data class LoginRequest(val email: String, val password: String)

/**
 * Registration takes an email (required) plus an optional username. When
 * [username] is null/blank the BE derives it from the email local-part.
 */
@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val username: String? = null,
)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class LogoutRequest(val refreshToken: String)

@Serializable
data class ChangePasswordRequest(val oldPassword: String, val newPassword: String)

@Serializable
data class AuthenticatedUser(
    val username: String,
    val name: String,
    val email: String,
    val emailVerified: Boolean = false,
    val groups: List<GroupSummary> = emptyList(),
)

@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: AuthenticatedUser,
)
