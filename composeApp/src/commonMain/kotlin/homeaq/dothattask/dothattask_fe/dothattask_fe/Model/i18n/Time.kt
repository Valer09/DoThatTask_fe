package homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Date / time helpers shared across the app. All formatters render in the
 * device's current timezone (`TimeZone.currentSystemDefault()`) so a backend
 * timestamp in UTC is always shown to the user as their local wall-clock
 * time, regardless of locale.
 *
 * Locale only flips the month name / `:` vs `.` separator etc. — for now we
 * keep the format independent of locale and simple (no dependency on
 * platform DateFormat) so it's deterministic across all targets.
 */
object Time {
    /**
     * Parse an ISO-8601 instant string (e.g. `2026-05-09T14:30:00Z`) and
     * format it as `dd/MM/yyyy HH:mm` in the device's current timezone.
     * Returns the input unchanged if it can't be parsed — callers should
     * never see an exception from a malformed timestamp.
     */
    fun formatLocal(iso: String, locale: AppLocale = LocaleManager.current): String {
        if (iso.isBlank()) return ""
        val parsed = runCatching { Instant.parse(iso).toLocalDateTime(TimeZone.currentSystemDefault()) }
            .getOrNull() ?: return iso
        return formatLocal(parsed, locale)
    }

    fun formatLocal(dateTime: LocalDateTime, locale: AppLocale = LocaleManager.current): String {
        val day = dateTime.dayOfMonth.pad2()
        val month = dateTime.monthNumber.pad2()
        val year = dateTime.year
        val hour = dateTime.hour.pad2()
        val min = dateTime.minute.pad2()
        return when (locale) {
            // Italian: "09/05/2026 14:30"
            AppLocale.Italian -> "$day/$month/$year $hour:$min"
            // English: "May 9, 2026 — 14:30" (24h, locale-neutral)
            AppLocale.English -> "${monthShortEn(dateTime.monthNumber)} ${dateTime.dayOfMonth}, $year — $hour:$min"
        }
    }
}

private fun Int.pad2(): String = if (this < 10) "0$this" else "$this"

private fun monthShortEn(m: Int): String = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)[m - 1]
