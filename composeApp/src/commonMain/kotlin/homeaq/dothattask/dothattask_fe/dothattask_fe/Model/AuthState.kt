package homeaq.dothattask.dothattask_fe.dothattask_fe.Model

import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group.GroupSummary
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.AuthProvider

/**
 * In-memory cache of the authenticated JWT session.
 * Persisted values live in [AuthProvider]; this object is the runtime mirror.
 */
object AuthState {
    // JWT session
    var username: String? = null
    var email: String? = null
    var accessToken: String? = null
    var refreshToken: String? = null
    var displayName: String? = null

    /**
     * Every group the user currently belongs to. Updated on login, register
     * and refresh from the backend's `AuthenticatedUser.groups`.
     */
    var groups: List<GroupSummary> = emptyList()

    /**
     * The group whose context is "currently active" — the X-Group-Id header
     * sent on group-scoped API calls. Selected by the user from the side menu;
     * defaults to the first available group on login.
     */
    var activeGroupId: Int? = null

    var onSessionExpired: (() -> Unit)? = null

    fun setSession(
        username: String,
        displayName: String,
        accessToken: String,
        refreshToken: String,
        groups: List<GroupSummary>,
        email: String,
    ) {
        this.username = username
        this.displayName = displayName
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.groups = groups
        this.email = email
        val current = activeGroupId
        activeGroupId = if (current != null && groups.any { it.id == current }) current
        else groups.firstOrNull()?.id

    }

    /** Load any previously-persisted session on app start. */
    fun loadFromStorage() {
        username = AuthProvider.getUsername()
        email = AuthProvider.getEmail()
        accessToken = AuthProvider.getAccessToken()
        refreshToken = AuthProvider.getRefreshToken()
        // groups/activeGroupId are restored after the first authenticated call
        // (validated /api/user/me) — no need to persist them on disk.
    }

    fun persist() {
        username?.let { AuthProvider.saveUsername(it) }
        email?.let { AuthProvider.saveEmail(it) }
        accessToken?.let { AuthProvider.saveAccessToken(it) }
        refreshToken?.let { AuthProvider.saveRefreshToken(it) }
    }

    fun clear() {
        username = null
        email = null
        accessToken = null
        refreshToken = null
        displayName = null
        groups = emptyList()
        activeGroupId = null
        AuthProvider.clearAll()
    }

    fun activeGroup(): GroupSummary? = activeGroupId?.let { id -> groups.firstOrNull { it.id == id } }

    val isLoggedIn: Boolean
        get() = accessToken != null
}
