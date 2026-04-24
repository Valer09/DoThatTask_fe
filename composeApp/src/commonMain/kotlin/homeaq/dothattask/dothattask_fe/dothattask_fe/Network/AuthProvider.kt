package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

/**
 * Platform-specific secure (or best-available) storage for the
 * authenticated session. Each platform persists values in a way
 * appropriate to its threat model — see the actual declarations.
 */
expect object AuthProvider {
    fun getUsername(): String?
    fun saveUsername(username: String)

    fun getAccessToken(): String?
    fun saveAccessToken(token: String)
    fun getRefreshToken(): String?
    fun saveRefreshToken(token: String)
    fun clearAll()
}
