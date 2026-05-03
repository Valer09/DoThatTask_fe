package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

fun networkError(e: Exception): ApiResult.Error = ApiResult.Error(e.message ?: "Unknown error", isNetwork = true)