package com.edrl.stickerbridge.core.conversion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FrameBudgetTest {
    @Test
    fun `a typical TikTok sticker keeps every frame`() {
        assertEquals(1, FrameBudget.keepEvery(width = 512, height = 512, frameCount = 48))
    }

    @Test
    fun `too many frames keep one in every few so they fit the budget`() {
        val keepEvery = FrameBudget.keepEvery(width = 512, height = 512, frameCount = 300)

        val kept = (300 + keepEvery - 1) / keepEvery
        assertTrue(keepEvery > 1)
        assertTrue(kept * FrameBudget.bytesPerFrame(512, 512) <= FrameBudget.MAX_DECODED_BYTES)
    }

    @Test
    fun `bytes per frame use four bytes per pixel`() {
        assertEquals(4L * 512 * 512, FrameBudget.bytesPerFrame(512, 512))
    }
}
