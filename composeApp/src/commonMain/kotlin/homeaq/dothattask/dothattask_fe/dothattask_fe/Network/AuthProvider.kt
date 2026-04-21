package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

/**
 * Platform-specific secure (or best-available) storage for the
 * authenticated session. Each platform persists values in a way
 * appropriate to its threat model — see the actual declarations.
 *
 * Legacy `token`/`username` methods are kept temporarily for callers
 * that have not yet migrated to the JWT flow. They will go away once
 * no screen relies on HTTP Basic anymore.
 */
expect object AuthProvider {
    // --- Legacy (HTTP Basic) -----------------------------------------------
    fun getToken(): String?
    fun saveToken(token: String)
    fun getUsername(): String?
    fun saveUsername(username: String)
    fun cleanToken()

    // --- JWT session -------------------------------------------------------
    fun getAccessToken(): String?
    fun saveAccessToken(token: String)
    fun getRefreshToken(): String?
    fun saveRefreshToken(token: String)
    fun clearAll()
}
