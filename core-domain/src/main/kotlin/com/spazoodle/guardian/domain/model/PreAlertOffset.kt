package com.spazoodle.guardian.domain.model

data class PreAlertOffset(
    val key: String,
    val offsetMillis: Long
)

object DefaultPreAlertOffsets {
    val ONE_DAY = PreAlertOffset("P1D", 24L * 60 * 60 * 1000)
    val ONE_HOUR = PreAlertOffset("PT1H", 60L * 60 * 1000)
    val TEN_MINUTES = PreAlertOffset("PT10M", 10L * 60 * 1000)
    val TWO_MINUTES = PreAlertOffset("PT2M", 2L * 60 * 1000)
}
