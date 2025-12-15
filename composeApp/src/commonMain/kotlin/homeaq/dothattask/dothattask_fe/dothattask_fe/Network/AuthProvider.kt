package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

expect object AuthProvider
{
    fun getToken(): String?
    fun getUsername(): String?
    fun saveToken(token: String)
    fun saveUsername(username: String)

    fun cleanToken()
}