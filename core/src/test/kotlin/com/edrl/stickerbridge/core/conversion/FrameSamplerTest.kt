package com.edrl.stickerbridge.core.conversion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FrameSamplerTest {
    @Test
    fun `original cadence keeps every frame and its duration`() {
        val frames = FrameSampler.sample(listOf(100, 50, 200), maxFps = null)

        assertEquals(listOf(SampledFrame(0, 100), SampledFrame(1, 50), SampledFrame(2, 200)), frames)
    }

    @Test
    fun `frames shorter than 8 ms are merged into the next one`() {
        val frames = FrameSampler.sample(listOf(100, 5, 200), maxFps = null)

        assertEquals(listOf(SampledFrame(0, 100), SampledFrame(2, 205)), frames)
    }

    @Test
    fun `a short last frame is merged into the previous one`() {
        val frames = FrameSampler.sample(listOf(100, 100, 4), maxFps = null)

        assertEquals(listOf(SampledFrame(0, 100), SampledFrame(1, 104)), frames)
    }

    @Test
    fun `limiting to 10 fps keeps the total duration`() {
        val frames = FrameSampler.sample(List(30) { 33L }, maxFps = 10)

        assertEquals(990L, frames.sumOf { it.durationMs })
        assertTrue(frames.dropLast(1).all { it.durationMs == 100L })
        assertEquals(listOf(0, 3, 6, 9, 12, 15, 18, 21, 24, 27), frames.map { it.sourceIndex })
    }

    @Test
    fun `limiting a cadence that is already lower changes nothing`() {
        val durations = listOf(200L, 200L, 200L)

        assertEquals(FrameSampler.sample(durations, maxFps = null), FrameSampler.sample(durations, maxFps = 15))
    }

    @Test
    fun `original cadence in frames per second`() {
        assertEquals(30.0, FrameSampler.fps(List(30) { 33L + if (it % 3 == 0) 1 else 0 }), 0.1)
    }

    @Test
    fun `no frame is ever shorter than the WhatsApp minimum`() {
        val frames = FrameSampler.sample(listOf(3, 3, 3, 3, 100), maxFps = null)

        assertTrue(frames.all { it.durationMs >= 8 })
        assertEquals(112L, frames.sumOf { it.durationMs })
    }
}
