package com.edrl.stickerbridge.core.extraction

import kotlin.test.Test
import kotlin.test.assertEquals

class LikeCountTest {
    @Test
    fun `counts under a thousand are shown as they are`() {
        assertEquals("0", LikeCount.compact(0))
        assertEquals("38", LikeCount.compact(38))
        assertEquals("999", LikeCount.compact(999))
    }

    @Test
    fun `thousands keep one decimal while it fits`() {
        assertEquals("1 mil", LikeCount.compact(1_000))
        assertEquals("3,3 mil", LikeCount.compact(3_340))
        assertEquals("58,1 mil", LikeCount.compact(58_094))
    }

    @Test
    fun `hundreds of thousands drop the decimal`() {
        assertEquals("168 mil", LikeCount.compact(168_241))
        assertEquals("420 mil", LikeCount.compact(419_937))
    }

    @Test
    fun `millions keep one decimal`() {
        assertEquals("1 M", LikeCount.compact(1_000_000))
        assertEquals("2,5 M", LikeCount.compact(2_540_000))
        assertEquals("120 M", LikeCount.compact(120_300_000))
    }

    @Test
    fun `rounding never shows a thousand thousands`() {
        assertEquals("1 M", LikeCount.compact(999_950))
        assertEquals("100 mil", LikeCount.compact(99_960))
    }

    @Test
    fun `negative counts are shown as zero`() {
        assertEquals("0", LikeCount.compact(-5))
    }
}
