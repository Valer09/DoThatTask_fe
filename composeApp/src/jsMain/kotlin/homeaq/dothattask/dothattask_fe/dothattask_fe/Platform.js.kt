package homeaq.dothattask.dothattask_fe.dothattask_fe

class JsPlatform : Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()