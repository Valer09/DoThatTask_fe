package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

/**
 * Per-platform persistence for the user's locale choice. Same pattern as
 * [OnboardingPreferences] — plain key/value, non-sensitive, survives logout.
 *
 * The stored value is a BCP-47-style tag ("en" or "it"). `null` means
 * "no explicit choice yet, fall back to the device system locale".
 */
expect object LocalePreferences {
    fun getLocale(): String?
    fun saveLocale(tag: String)
}

/** Returns the device's preferred language as a tag like "en" or "it". */
expect fun detectSystemLocale(): String?
