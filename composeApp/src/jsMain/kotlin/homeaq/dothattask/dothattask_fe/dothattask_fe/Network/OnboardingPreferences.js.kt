package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

actual object OnboardingPreferences {
    private const val KEY_SEEN = "onboarding_seen"

    actual fun hasSeenOnboarding(): Boolean = localStorage[KEY_SEEN] == "true"

    actual fun markOnboardingSeen() {
        localStorage[KEY_SEEN] = "true"
    }
}
