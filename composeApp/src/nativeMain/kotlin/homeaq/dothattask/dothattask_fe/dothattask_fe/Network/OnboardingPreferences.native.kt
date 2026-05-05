package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import platform.Foundation.NSUserDefaults

actual object OnboardingPreferences {
    private const val KEY_SEEN = "onboarding_seen"

    actual fun hasSeenOnboarding(): Boolean =
        NSUserDefaults.standardUserDefaults.boolForKey(KEY_SEEN)

    actual fun markOnboardingSeen() {
        NSUserDefaults.standardUserDefaults.setBool(true, KEY_SEEN)
    }
}
