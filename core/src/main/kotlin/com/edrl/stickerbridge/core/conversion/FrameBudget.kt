package com.edrl.stickerbridge.core.conversion

/**
 * Keeps the decoded frames of one animation within a memory budget. The frame count is known
 * before decoding but the durations are not, so the budget is expressed as "keep one frame in
 * every N"; a skipped frame's time goes to the frame kept before it.
 */
object FrameBudget {
    const val MAX_DECODED_BYTES = 96L * 1024 * 1024
    private const val BYTES_PER_PIXEL = 4L

    fun bytesPerFrame(
        width: Int,
        height: Int,
    ): Long = BYTES_PER_PIXEL * width * height

    /** 1 when every frame fits; otherwise the smallest N such that one in every N frames fits. */
    fun keepEvery(
        width: Int,
        height: Int,
        frameCount: Int,
    ): Int {
        val maxFrames = (MAX_DECODED_BYTES / bytesPerFrame(width, height)).coerceAtLeast(1)
        return ((frameCount + maxFrames - 1) / maxFrames).toInt().coerceAtLeast(1)
    }
}
