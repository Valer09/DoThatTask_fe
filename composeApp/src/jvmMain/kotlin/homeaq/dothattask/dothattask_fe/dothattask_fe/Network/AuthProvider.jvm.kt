package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import java.io.File
import java.util.Properties

/**
 * Desktop storage: plaintext properties file under ~/.dothattask/.
 * Acceptable MVP trade-off — a local attacker with read access to the
 * user's home directory can read tokens. Document and revisit with
 * JNA-backed OS keychains if the desktop target gains real users.
 */
actual object AuthProvider {
    private const val KEY_USERNAME = "username"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"

    private val file: File by lazy {
        val home = System.getProperty("user.home") ?: "."
        File(home, ".dothattask/tokens.properties").apply { parentFile?.mkdirs() }
    }

    private fun load(): Properties {
        val p = Properties()
        if (file.exists()) file.inputStream().use { p.load(it) }
        return p
    }

    private fun put(key: String, value: String) {
        val p = load()
        p.setProperty(key, value)
        file.outputStream().use { p.store(it, null) }
    }

    private fun get(key: String): String? = load().getProperty(key)
    actual fun getUsername(): String? = get(KEY_USERNAME)
    actual fun saveUsername(username: String) = put(KEY_USERNAME, username)
    actual fun getAccessToken(): String? = get(KEY_ACCESS)
    actual fun saveAccessToken(token: String) = put(KEY_ACCESS, token)
    actual fun getRefreshToken(): String? = get(KEY_REFRESH)
    actual fun saveRefreshToken(token: String) = put(KEY_REFRESH, token)
    actual fun clearAll() {
        if (file.exists()) file.delete()
    }
}
