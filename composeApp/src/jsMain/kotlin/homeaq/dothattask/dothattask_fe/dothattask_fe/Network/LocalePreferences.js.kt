package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.set

actual object LocalePreferences {
    private const val KEY_LOCALE = "locale_tag"

    actual fun getLocale(): String? = localStorage[KEY_LOCALE]

    actual fun saveLocale(tag: String) {
        localStorage[KEY_LOCALE] = tag
    }
}

actual fun detectSystemLocale(): String? = window.navigator.language
