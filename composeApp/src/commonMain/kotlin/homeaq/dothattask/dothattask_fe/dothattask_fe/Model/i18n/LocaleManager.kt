package homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.LocalePreferences
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.detectSystemLocale

enum class AppLocale(val tag: String) {
    English("en"),
    Italian("it");

    companion object {
        /**
         * Resolve a locale from a BCP-47ish tag (or any string starting with
         * "it"/"en"). Falls back to English on anything we don't recognise.
         */
        fun fromTag(tag: String?): AppLocale {
            val lower = tag?.lowercase().orEmpty()
            return when {
                lower.startsWith("it") -> Italian
                else -> English
            }
        }
    }
}

/**
 * Runtime locale switch. Reads the user's stored choice on first access;
 * if none is set, defaults to the device system locale (Italian if the OS
 * is Italian, English otherwise — see [AppLocale.fromTag]).
 *
 * The `current` MutableState is the source of truth for composables: any
 * Composable that calls [stringsForCurrentLocale] (directly or via
 * `LocalStrings.current`) recomposes when `current` changes.
 */
object LocaleManager {
    private var currentState = mutableStateOf(initialLocale())

    val current: AppLocale get() = currentState.value

    fun set(locale: AppLocale) {
        currentState.value = locale
        LocalePreferences.saveLocale(locale.tag)
    }

    private fun initialLocale(): AppLocale {
        val saved = LocalePreferences.getLocale()
        return if (!saved.isNullOrBlank()) AppLocale.fromTag(saved)
        else AppLocale.fromTag(detectSystemLocale())
    }
}

fun stringsFor(locale: AppLocale): Strings = when (locale) {
    AppLocale.English -> EnglishStrings
    AppLocale.Italian -> ItalianStrings
}

/**
 * CompositionLocal that delivers the currently-selected [Strings] bundle
 * to every Composable below the LocaleProvider in the tree. The default
 * (English) is just a safety net — `App.kt` always installs the real one
 * keyed by [LocaleManager.current].
 */
val LocalStrings = compositionLocalOf { EnglishStrings }
