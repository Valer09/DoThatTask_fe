package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import java.io.File
import java.util.Properties

actual object OnboardingPreferences {
    private const val KEY_SEEN = "seen"

    private val file: File by lazy {
        val home = System.getProperty("user.home") ?: "."
        File(home, ".dothattask/onboarding.properties").apply { parentFile?.mkdirs() }
    }

    private fun load(): Properties {
        val p = Properties()
        if (file.exists()) file.inputStream().use { p.load(it) }
        return p
    }

    actual fun hasSeenOnboarding(): Boolean = load().getProperty(KEY_SEEN) == "true"

    actual fun markOnboardingSeen() {
        val p = load()
        p.setProperty(KEY_SEEN, "true")
        file.outputStream().use { p.store(it, null) }
    }
}
