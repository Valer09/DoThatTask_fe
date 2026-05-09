package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import android.content.Context
import androidx.core.content.edit
import java.util.Locale

actual object LocalePreferences {
    private const val PREFS = "locale_prefs"
    private const val KEY_LOCALE = "tag"

    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
    }

    private fun prefs() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    actual fun getLocale(): String? = prefs().getString(KEY_LOCALE, null)

    actual fun saveLocale(tag: String) {
        prefs().edit { putString(KEY_LOCALE, tag) }
    }
}

actual fun detectSystemLocale(): String? = Locale.getDefault().language
