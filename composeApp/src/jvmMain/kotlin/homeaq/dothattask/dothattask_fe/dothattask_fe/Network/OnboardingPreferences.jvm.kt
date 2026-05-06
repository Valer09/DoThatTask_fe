package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import java.io.File
import java.util.Properties

actual object OnboardingPreferences {
    private const val KEY_SEEN = "seen"
    private const val KEY_FEATURES = "features_seen"

    private val file: File by lazy {
        val home = System.getProperty("user.home") ?: "."
        File(home, ".dothattask/onboarding.properties").apply { parentFile?.mkdirs() }
    }

    private fun load(): Properties {
        val p = Properties()
        if (file.exists()) file.inputStream().use { p.load(it) }
        return p
    }

    private fun put(key: String, value: String) {
        val p = load()
        p.setProperty(key, value)
        file.outputStream().use { p.store(it, null) }
    }

    actual fun hasSeenOnboarding(): Boolean = load().getProperty(KEY_SEEN) == "true"

    actual fun markOnboardingSeen() = put(KEY_SEEN, "true")

    actual fun hasSeenFeatures(): Boolean = load().getProperty(KEY_FEATURES) == "true"

    actual fun markFeaturesSeen() = put(KEY_FEATURES, "true")
}
