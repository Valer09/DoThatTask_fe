package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

actual object AuthProvider {
    actual fun getToken(): String? {
        return ""
    }

    actual fun saveToken(token: String) {
    }

    actual fun cleanToken() {
    }

    actual fun saveUsername(username: String){}

    actual fun getUsername(): String? {return null}
}