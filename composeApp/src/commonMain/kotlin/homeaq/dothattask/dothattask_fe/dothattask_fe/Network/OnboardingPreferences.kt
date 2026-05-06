package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

/**
 * Tiny per-platform key/value store for the two onboarding flags:
 *  - `onboarding_seen` — the pre-login carousel that pitches the app idea.
 *  - `features_seen`   — the post-login carousel that walks the user
 *    through each tab once they're authenticated.
 *
 * Kept separate from [AuthProvider] because (a) the values are non-sensitive —
 * plain SharedPreferences/localStorage/UserDefaults are fine, no need for
 * the encrypted backing — and (b) they must survive a logout, so they
 * can't live next to tokens which get wiped on `clearAll`.
 */
expect object OnboardingPreferences {
    fun hasSeenOnboarding(): Boolean
    fun markOnboardingSeen()

    fun hasSeenFeatures(): Boolean
    fun markFeaturesSeen()
}
