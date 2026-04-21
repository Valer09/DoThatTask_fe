package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import android.content.Context
import homeaq.dothattask.dothattask_fe.dothattask_fe.AuthStorage

actual object AuthProvider {
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
    }

    actual fun getToken(): String? = AuthStorage.getToken(context)
    actual fun saveToken(token: String) = AuthStorage.saveToken(context, token)
    actual fun cleanToken() = AuthStorage.clear(context)
    actual fun saveUsername(username: String) = AuthStorage.saveUsername(context, username)
    actual fun getUsername(): String? = AuthStorage.getUsername(context)

    actual fun getAccessToken(): String? = AuthStorage.getAccessToken(context)
    actual fun saveAccessToken(token: String) = AuthStorage.saveAccessToken(context, token)
    actual fun getRefreshToken(): String? = AuthStorage.getRefreshToken(context)
    actual fun saveRefreshToken(token: String) = AuthStorage.saveRefreshToken(context, token)
    actual fun clearAll() = AuthStorage.clear(context)
}
