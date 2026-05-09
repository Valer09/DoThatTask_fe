package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.preferredLanguages

actual object LocalePreferences {
    private const val KEY_LOCALE = "locale_tag"

    actual fun getLocale(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(KEY_LOCALE)

    actual fun saveLocale(tag: String) {
        NSUserDefaults.standardUserDefaults.setObject(tag, KEY_LOCALE)
    }
}

actual fun detectSystemLocale(): String? {
    // `preferredLanguages` is the user's ordered list (e.g. ["it-IT", "en"]).
    // `languageCode` on currentLocale also works but is sometimes empty on
    // older simulators — preferredLanguages is more reliable.
    val first = NSLocale.preferredLanguages.firstOrNull() as? String
    return first ?: NSLocale.currentLocale.languageCode
}
