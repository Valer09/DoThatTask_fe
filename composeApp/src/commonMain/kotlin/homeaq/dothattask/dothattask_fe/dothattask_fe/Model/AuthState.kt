package homeaq.dothattask.dothattask_fe.dothattask_fe.Model

import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthProvider
import io.ktor.util.encodeBase64

/**
 * In-memory cache of the authenticated session. Persisted values
 * live in [AuthProvider]; this object is the runtime mirror.
 *
 * Legacy Basic-auth fields (`token`, `password`) are kept while the
 * UI finishes migrating to JWT — remove once no screen calls
 * `setCredentials`.
 */
object AuthState {
    // Legacy HTTP Basic
    var username: String? = null
    var password: String? = null
    var token: String? = null

    // JWT session
    var accessToken: String? = null
    var refreshToken: String? = null
    var groupId: Int? = null
    var displayName: String? = null

    fun setCredentials(user: String, pass: String) {
        username = user
        password = pass
        token = "$user:$pass".encodeToByteArray().encodeBase64()
    }

    fun setSession(
        username: String,
        displayName: String,
        accessToken: String,
        refreshToken: String,
        groupId: Int?,
    ) {
        this.username = username
        this.displayName = displayName
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.groupId = groupId
    }

    /** Load any previously-persisted session on app start. */
    fun loadFromStorage() {
        username = AuthProvider.getUsername()
        accessToken = AuthProvider.getAccessToken()
        refreshToken = AuthProvider.getRefreshToken()
    }

    fun persist() {
        username?.let { AuthProvider.saveUsername(it) }
        accessToken?.let { AuthProvider.saveAccessToken(it) }
        refreshToken?.let { AuthProvider.saveRefreshToken(it) }
    }

    fun clear() {
        username = null
        password = null
        token = null
        accessToken = null
        refreshToken = null
        groupId = null
        displayName = null
        AuthProvider.clearAll()
    }

    val isLoggedIn: Boolean
        get() = accessToken != null || token != null
}
