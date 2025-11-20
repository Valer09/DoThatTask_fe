package homeaq.dothattask.dothattask_fe.dothattask_fe

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform