package com.edrl.stickerbridge.core.conversion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FrameBudgetTest {
    @Test
    fun `a typical TikTok sticker fits without reducing its cadence`() {
        assertNull(FrameBudget.decodeFps(width = 498, height = 498, frameDurationsMs = List(48) { 41L }))
    }

    @Test
    fun `too many frames get a cadence that fits the budget`() {
        val durations = List(300) { 33L }

        val fps = assertNotNull(FrameBudget.decodeFps(width = 498, height = 498, frameDurationsMs = durations))

        val kept = FrameSampler.sample(durations, fps).size
        assertTrue(kept * FrameBudget.bytesPerFrame(498, 498) <= FrameBudget.MAX_DECODED_BYTES)
    }

    @Test
    fun `bytes per frame use four bytes per pixel`() {
        assertEquals(4L * 512 * 512, FrameBudget.bytesPerFrame(512, 512))
    }
}
