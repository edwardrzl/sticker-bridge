package com.edrl.stickerbridge.core.conversion

/** One animated encoding attempt; a null [maxFps] keeps the source cadence. */
data class AnimatedAttempt(
    val quality: Int,
    val maxFps: Int?,
)

/**
 * The animated attempts, in order, until one fits WhatsApp's size limit (BR-09): first lower
 * quality at the original cadence, then fewer frames per second. Kept short so a conversion
 * stays within a few seconds (NFR2).
 */
object AnimatedQualityLadder {
    @Suppress("MagicNumber")
    val ATTEMPTS =
        listOf(
            AnimatedAttempt(quality = 75, maxFps = null),
            AnimatedAttempt(quality = 50, maxFps = null),
            AnimatedAttempt(quality = 30, maxFps = null),
            AnimatedAttempt(quality = 50, maxFps = 15),
            AnimatedAttempt(quality = 30, maxFps = 15),
            AnimatedAttempt(quality = 40, maxFps = 10),
            AnimatedAttempt(quality = 30, maxFps = 10),
        )

    /** The attempts that make sense for a source: limits at or above its cadence change nothing. */
    fun attemptsFor(frameDurationsMs: List<Long>): List<AnimatedAttempt> {
        val sourceFps = FrameSampler.fps(frameDurationsMs)
        return ATTEMPTS.filter { it.maxFps == null || it.maxFps < sourceFps }
    }
}
