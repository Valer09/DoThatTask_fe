package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

/**
 * Tiny per-platform key/value store for "did the user already see the
 * onboarding carousel?". Kept separate from [AuthProvider] because (a) the
 * value is non-sensitive — plain SharedPreferences/localStorage/UserDefaults
 * are fine, no need for the encrypted backing — and (b) it must survive
 * a logout, so it can't live next to tokens which get wiped on `clearAll`.
 */
expect object OnboardingPreferences {
    fun hasSeenOnboarding(): Boolean
    fun markOnboardingSeen()
}
