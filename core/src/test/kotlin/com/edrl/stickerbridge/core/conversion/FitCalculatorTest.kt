package com.edrl.stickerbridge.core.conversion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FitCalculatorTest {
    @Test
    fun `portrait image fills the height and is centered horizontally`() {
        assertEquals(Placement(width = 256, height = 512, offsetX = 128, offsetY = 0), FitCalculator.fit(300, 600))
    }

    @Test
    fun `landscape image fills the width and is centered vertically`() {
        assertEquals(Placement(width = 512, height = 256, offsetX = 0, offsetY = 128), FitCalculator.fit(800, 400))
    }

    @Test
    fun `small square image is scaled up to the whole canvas`() {
        assertEquals(Placement(width = 512, height = 512, offsetX = 0, offsetY = 0), FitCalculator.fit(100, 100))
    }

    @Test
    fun `very thin image keeps at least one pixel`() {
        val placement = FitCalculator.fit(5000, 1)

        assertEquals(512, placement.width)
        assertEquals(1, placement.height)
    }

    @Test
    fun `rejects non positive sizes`() {
        assertFailsWith<IllegalArgumentException> { FitCalculator.fit(0, 10) }
    }
}
