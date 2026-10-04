package com.edrl.stickerbridge.core.extraction

enum class LoadDecision { Wait, Done, TimedOut }

/**
 * When the initial load of comments ends (BR-03, BR-04): it times out without a first batch in
 * 30 s, and ends after 3 batches, when TikTok has no more comments, or 10 s after the first batch.
 * Times are passed in, so the rule needs no clock.
 */
object InitialLoadPolicy {
    const val FIRST_BATCH_TIMEOUT_MS = 30_000L
    const val WINDOW_AFTER_FIRST_BATCH_MS = 10_000L
    const val MAX_BATCHES = 3

    fun decide(
        batches: Int,
        hasMore: Boolean,
        sinceStart: Long,
        sinceFirstBatch: Long?,
    ): LoadDecision =
        when {
            batches == 0 -> if (sinceStart >= FIRST_BATCH_TIMEOUT_MS) LoadDecision.TimedOut else LoadDecision.Wait
            batches >= MAX_BATCHES || !hasMore -> LoadDecision.Done
            (sinceFirstBatch ?: 0) >= WINDOW_AFTER_FIRST_BATCH_MS -> LoadDecision.Done
            else -> LoadDecision.Wait
        }

    /** How long to wait for the next batch before deciding again. */
    fun remainingMs(
        batches: Int,
        sinceStart: Long,
        sinceFirstBatch: Long?,
    ): Long {
        val remaining =
            if (batches == 0) {
                FIRST_BATCH_TIMEOUT_MS - sinceStart
            } else {
                WINDOW_AFTER_FIRST_BATCH_MS - (sinceFirstBatch ?: 0)
            }
        return remaining.coerceAtLeast(0)
    }
}
