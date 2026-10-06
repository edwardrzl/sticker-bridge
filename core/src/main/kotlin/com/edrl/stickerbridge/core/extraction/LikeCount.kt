package com.edrl.stickerbridge.core.extraction

import kotlin.math.roundToLong

/** Writes a like count the short way people read it in Spanish: 38, 3,3 mil, 420 mil, 2,5 M. */
object LikeCount {
    private const val THOUSAND = 1_000L
    private const val MILLION = 1_000_000L
    private const val TENTHS = 10

    /** From this many units on, the decimal is dropped: 168 mil, not 168,2 mil. */
    private const val WHOLE_FROM = 100

    fun compact(likes: Long): String {
        val count = likes.coerceAtLeast(0)
        val thousands = scaled(count, THOUSAND)
        return when {
            count < THOUSAND -> count.toString()
            thousands < THOUSAND * TENTHS -> "${text(thousands)} mil"
            else -> "${text(scaled(count, MILLION))} M"
        }
    }

    /** [count] in [unit]s as tenths, rounded: whole units from [WHOLE_FROM] on, one decimal below. */
    private fun scaled(
        count: Long,
        unit: Long,
    ): Long {
        val tenths = (count * TENTHS / unit.toDouble()).roundToLong()
        return if (tenths >= WHOLE_FROM * TENTHS) (tenths / TENTHS.toDouble()).roundToLong() * TENTHS else tenths
    }

    private fun text(tenths: Long): String {
        val whole = tenths / TENTHS
        val decimal = tenths % TENTHS
        return if (decimal == 0L) whole.toString() else "$whole,$decimal"
    }
}
