package homeaq.dothattask.dothattask_fe.dothattask_fe

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()