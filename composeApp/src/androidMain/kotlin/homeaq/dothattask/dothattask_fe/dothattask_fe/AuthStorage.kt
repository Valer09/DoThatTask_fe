package homeaq.dothattask.dothattask_fe.dothattask_fe

import android.content.Context
import android.util.Base64
import androidx.core.content.edit

object AuthStorage {
    private const val PREFS = "auth_prefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_USERNAME_IV = "username_iv"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_ACCESS_IV = "access_token_iv"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_REFRESH_IV = "refresh_token_iv"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun encryptAndStore(context: Context, plain: String, keyName: String, ivName: String) {
        val key = KeyStoreHelper.getOrCreateKey()
        val (encrypted, iv) = CryptoHelper.encrypt(plain, key)
        prefs(context).edit {
            putString(keyName, Base64.encodeToString(encrypted, Base64.DEFAULT))
            putString(ivName, Base64.encodeToString(iv, Base64.DEFAULT))
        }
    }

    private fun loadAndDecrypt(context: Context, keyName: String, ivName: String): String? {
        val key = KeyStoreHelper.getOrCreateKey()
        val encryptedStr = prefs(context).getString(keyName, null) ?: return null
        val ivStr = prefs(context).getString(ivName, null) ?: return null
        val encrypted = Base64.decode(encryptedStr, Base64.DEFAULT)
        val iv = Base64.decode(ivStr, Base64.DEFAULT)
        return CryptoHelper.decrypt(encrypted, iv, key)
    }

    fun saveUsername(context: Context, username: String) =
        encryptAndStore(context, username, KEY_USERNAME, KEY_USERNAME_IV)

    fun getUsername(context: Context): String? =
        loadAndDecrypt(context, KEY_USERNAME, KEY_USERNAME_IV)

    fun saveAccessToken(context: Context, token: String) =
        encryptAndStore(context, token, KEY_ACCESS, KEY_ACCESS_IV)

    fun getAccessToken(context: Context): String? =
        loadAndDecrypt(context, KEY_ACCESS, KEY_ACCESS_IV)

    fun saveRefreshToken(context: Context, token: String) =
        encryptAndStore(context, token, KEY_REFRESH, KEY_REFRESH_IV)

    fun getRefreshToken(context: Context): String? =
        loadAndDecrypt(context, KEY_REFRESH, KEY_REFRESH_IV)

    fun clear(context: Context) {
        prefs(context).edit { clear() }
    }
}
