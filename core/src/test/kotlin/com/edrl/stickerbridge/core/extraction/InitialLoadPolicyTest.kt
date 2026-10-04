package com.edrl.stickerbridge.core.extraction

import kotlin.test.Test
import kotlin.test.assertEquals

class InitialLoadPolicyTest {
    private fun decide(
        batches: Int,
        hasMore: Boolean = true,
        sinceStart: Long = 0,
        sinceFirstBatch: Long? = null,
    ) = InitialLoadPolicy.decide(batches, hasMore, sinceStart, sinceFirstBatch)

    @Test
    fun `waits for the first batch up to thirty seconds`() {
        assertEquals(LoadDecision.Wait, decide(batches = 0, sinceStart = 29_999))
        assertEquals(LoadDecision.TimedOut, decide(batches = 0, sinceStart = 30_000))
    }

    @Test
    fun `stops after three batches`() {
        assertEquals(LoadDecision.Done, decide(batches = 3, sinceStart = 5_000, sinceFirstBatch = 2_000))
    }

    @Test
    fun `stops when TikTok has no more comments`() {
        assertEquals(LoadDecision.Done, decide(batches = 1, hasMore = false, sinceStart = 4_000, sinceFirstBatch = 0))
    }

    @Test
    fun `stops ten seconds after the first batch`() {
        assertEquals(LoadDecision.Wait, decide(batches = 2, sinceStart = 15_000, sinceFirstBatch = 9_999))
        assertEquals(LoadDecision.Done, decide(batches = 2, sinceStart = 15_000, sinceFirstBatch = 10_000))
    }

    @Test
    fun `remaining wait follows the current deadline`() {
        assertEquals(26_000L, InitialLoadPolicy.remainingMs(batches = 0, sinceStart = 4_000, sinceFirstBatch = null))
        assertEquals(7_000L, InitialLoadPolicy.remainingMs(batches = 1, sinceStart = 9_000, sinceFirstBatch = 3_000))
        assertEquals(0L, InitialLoadPolicy.remainingMs(batches = 1, sinceStart = 40_000, sinceFirstBatch = 12_000))
    }
}
