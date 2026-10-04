package com.edrl.stickerbridge.core.conversion

/**
 * Keeps the decoded frames of one animation within a memory budget: when all frames would not
 * fit, they are decoded at a lower cadence chosen here.
 */
object FrameBudget {
    const val MAX_DECODED_BYTES = 96L * 1024 * 1024
    private const val BYTES_PER_PIXEL = 4L
    private const val MS_PER_SECOND = 1000L

    fun bytesPerFrame(
        width: Int,
        height: Int,
    ): Long = BYTES_PER_PIXEL * width * height

    /** The cadence to decode at, or null when every frame fits in the budget. */
    fun decodeFps(
        width: Int,
        height: Int,
        frameDurationsMs: List<Long>,
    ): Int? {
        val perFrame = bytesPerFrame(width, height)
        if (perFrame * frameDurationsMs.size <= MAX_DECODED_BYTES) return null
        val maxFrames = MAX_DECODED_BYTES / perFrame
        val total = frameDurationsMs.sum().coerceAtLeast(1)
        return (maxFrames * MS_PER_SECOND / total).toInt().coerceAtLeast(1)
    }
}
