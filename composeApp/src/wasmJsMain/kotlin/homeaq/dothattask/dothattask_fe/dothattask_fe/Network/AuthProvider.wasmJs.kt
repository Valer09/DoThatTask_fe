package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * Web storage: browser localStorage. Tokens are reachable by any
 * script that runs on the same origin, so XSS can exfiltrate them.
 * Accepted MVP trade-off; document in README.
 */
actual object AuthProvider {
    private const val KEY_USERNAME = "username"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"

    actual fun getUsername(): String? = localStorage[KEY_USERNAME]
    actual fun saveUsername(username: String) { localStorage[KEY_USERNAME] = username }
    actual fun getAccessToken(): String? = localStorage[KEY_ACCESS]
    actual fun saveAccessToken(token: String) { localStorage[KEY_ACCESS] = token }
    actual fun getRefreshToken(): String? = localStorage[KEY_REFRESH]
    actual fun saveRefreshToken(token: String) { localStorage[KEY_REFRESH] = token }

    actual fun clearAll() {
        localStorage.removeItem(KEY_USERNAME)
        localStorage.removeItem(KEY_ACCESS)
        localStorage.removeItem(KEY_REFRESH)
    }
}

