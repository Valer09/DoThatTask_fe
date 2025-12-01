package homeaq.dothattask.dothattask_fe.dothattask_fe.Model

import io.ktor.util.encodeBase64

object AuthState {
    var username: String? = null
    var password: String? = null
    var token: String? = null

    fun setCredentials(user: String, pass: String) {
        username = user
        password = pass
        token = "$user:$pass".encodeToByteArray().encodeBase64()
    }

    fun clear() {
        username = null
        password = null
        token = null
    }

    val isLoggedIn: Boolean
        get() = token != null
}