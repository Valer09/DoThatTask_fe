package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T, val message: String = "Completed!") : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}