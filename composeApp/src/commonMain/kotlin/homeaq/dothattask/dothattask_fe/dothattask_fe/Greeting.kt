package homeaq.dothattask.dothattask_fe.dothattask_fe

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}