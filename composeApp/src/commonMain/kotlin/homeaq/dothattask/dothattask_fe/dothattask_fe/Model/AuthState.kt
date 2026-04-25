package homeaq.dothattask.dothattask_fe.dothattask_fe.Model

import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthProvider

/**
 * In-memory cache of the authenticated JWT session.
 * Persisted values live in [AuthProvider]; this object is the runtime mirror.
 */
object AuthState {
    // JWT session
    var username: String? = null
    var accessToken: String? = null
    var refreshToken: String? = null
    var groupId: Int? = null
    var displayName: String? = null

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
        accessToken = null
        refreshToken = null
        groupId = null
        displayName = null
        AuthProvider.clearAll()
    }

    val isLoggedIn: Boolean
        get() = accessToken != null
}
