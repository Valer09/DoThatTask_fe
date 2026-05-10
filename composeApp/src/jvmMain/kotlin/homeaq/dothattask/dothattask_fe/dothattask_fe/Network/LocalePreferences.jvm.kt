package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import java.io.File
import java.util.Locale
import java.util.Properties

actual object LocalePreferences {
    private const val KEY_LOCALE = "tag"

    private val file: File by lazy {
        val home = System.getProperty("user.home") ?: "."
        File(home, ".dothattask/locale.properties").apply { parentFile?.mkdirs() }
    }

    private fun load(): Properties {
        val p = Properties()
        if (file.exists()) file.inputStream().use { p.load(it) }
        return p
    }

    actual fun getLocale(): String? = load().getProperty(KEY_LOCALE)

    actual fun saveLocale(tag: String) {
        val p = load()
        p.setProperty(KEY_LOCALE, tag)
        file.outputStream().use { p.store(it, null) }
    }
}

actual fun detectSystemLocale(): String? = Locale.getDefault().language
