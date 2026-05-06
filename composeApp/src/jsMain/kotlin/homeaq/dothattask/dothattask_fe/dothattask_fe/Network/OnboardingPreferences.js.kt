package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

actual object OnboardingPreferences {
    private const val KEY_SEEN = "onboarding_seen"
    private const val KEY_FEATURES = "features_seen"

    actual fun hasSeenOnboarding(): Boolean = localStorage[KEY_SEEN] == "true"

    actual fun markOnboardingSeen() {
        localStorage[KEY_SEEN] = "true"
    }

    actual fun hasSeenFeatures(): Boolean = localStorage[KEY_FEATURES] == "true"

    actual fun markFeaturesSeen() {
        localStorage[KEY_FEATURES] = "true"
    }
}
