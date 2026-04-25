package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import platform.Foundation.NSUserDefaults

/**
 * iOS storage: backed by NSUserDefaults for the MVP. Values live in
 * the app's sandboxed preferences plist, which is readable only by
 * this app. A proper Keychain backing is a follow-up task.
 * Switching to Keychain changes only this file.
 */
actual object AuthProvider {
    private const val KEY_USERNAME = "username"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"

    private fun defaults(): NSUserDefaults = NSUserDefaults.standardUserDefaults

    private fun put(key: String, value: String) = defaults().setObject(value, key)
    private fun get(key: String): String? = defaults().stringForKey(key)
    private fun remove(key: String) = defaults().removeObjectForKey(key)
    actual fun getUsername(): String? = get(KEY_USERNAME)
    actual fun saveUsername(username: String) = put(KEY_USERNAME, username)
    actual fun getAccessToken(): String? = get(KEY_ACCESS)
    actual fun saveAccessToken(token: String) = put(KEY_ACCESS, token)
    actual fun getRefreshToken(): String? = get(KEY_REFRESH)
    actual fun saveRefreshToken(token: String) = put(KEY_REFRESH, token)

    actual fun clearAll() {
        remove(KEY_USERNAME)
        remove(KEY_ACCESS)
        remove(KEY_REFRESH)
    }
}
