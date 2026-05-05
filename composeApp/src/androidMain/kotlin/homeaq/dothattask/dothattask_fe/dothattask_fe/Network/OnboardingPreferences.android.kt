package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import android.content.Context
import androidx.core.content.edit

actual object OnboardingPreferences {
    private const val PREFS = "onboarding_prefs"
    private const val KEY_SEEN = "seen"

    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
    }

    private fun prefs() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    actual fun hasSeenOnboarding(): Boolean = prefs().getBoolean(KEY_SEEN, false)

    actual fun markOnboardingSeen() {
        prefs().edit { putBoolean(KEY_SEEN, true) }
    }
}
