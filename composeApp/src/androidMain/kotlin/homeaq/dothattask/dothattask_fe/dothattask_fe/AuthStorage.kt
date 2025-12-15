package homeaq.dothattask.dothattask_fe.dothattask_fe

import android.content.Context
import android.util.Base64
import androidx.core.content.edit

object AuthStorage {
    private const val PREFS = "auth_prefs"
    private const val KEY_TOKEN = "token"
    private const val KEY_USERNAME = "username"
    private const val KEY_TOKEN_IV = "token_iv"
    private const val KEY_USERNAME_IV = "username_iv"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun saveToken(context: Context, token: String) {
        val key = KeyStoreHelper.getOrCreateKey()
        val (encrypted, iv) = CryptoHelper.encrypt(token, key)

        prefs(context).edit {
            putString(KEY_TOKEN, Base64.encodeToString(encrypted, Base64.DEFAULT))
            putString(KEY_TOKEN_IV, Base64.encodeToString(iv, Base64.DEFAULT))
        }
    }

    fun getToken(context: Context): String? {
        val key = KeyStoreHelper.getOrCreateKey()
        val encryptedStr = prefs(context).getString(KEY_TOKEN, null) ?: return null
        val ivStr = prefs(context).getString(KEY_TOKEN_IV, null) ?: return null

        val encrypted = Base64.decode(encryptedStr, Base64.DEFAULT)
        val iv = Base64.decode(ivStr, Base64.DEFAULT)

        return CryptoHelper.decrypt(encrypted, iv, key)
    }

    fun clear(context: Context) {
        prefs(context).edit { clear() }
    }

    fun saveUsername(context: Context, username: String) {

        val key = KeyStoreHelper.getOrCreateKey()
        val (encrypted, iv) = CryptoHelper.encrypt(username, key)

        prefs(context).edit {
            putString(KEY_USERNAME, Base64.encodeToString(encrypted, Base64.DEFAULT))
            putString(KEY_USERNAME_IV, Base64.encodeToString(iv, Base64.DEFAULT))
        }
    }

    fun getUsername(context: Context): String?
    {
        val key = KeyStoreHelper.getOrCreateKey()
        val encryptedStr = prefs(context).getString(KEY_USERNAME, null) ?: return null
        val ivStr = prefs(context).getString(KEY_USERNAME_IV, null) ?: return null

        val encrypted = Base64.decode(encryptedStr, Base64.DEFAULT)
        val iv = Base64.decode(ivStr, Base64.DEFAULT)

        return CryptoHelper.decrypt(encrypted, iv, key)
    }
}
