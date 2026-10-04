package com.edrl.stickerbridge.core.conversion

import com.edrl.stickerbridge.core.pack.StickerLimits
import kotlin.math.ceil
import kotlin.math.roundToLong

/**
 * Chooses the frames of an output animation from the source frame durations (BR-09). The total
 * duration is always preserved: the animation is never shortened or sped up.
 */
object FrameSampler {
    private const val MS_PER_SECOND = 1000.0

    /** Frames per second of the source, from its frame durations. */
    fun fps(durationsMs: List<Long>): Double {
        val total = durationsMs.sum()
        return if (total <= 0) 0.0 else durationsMs.size * MS_PER_SECOND / total
    }

    /**
     * The frames to encode. With [maxFps] null (or when the source is already at most that
     * cadence), every source frame is kept; otherwise the timeline is sampled every `1000 / maxFps`
     * milliseconds. No output frame is shorter than WhatsApp's minimum frame duration.
     */
    fun sample(
        durationsMs: List<Long>,
        maxFps: Int?,
    ): List<SampledFrame> {
        val raw =
            if (maxFps == null || fps(durationsMs) <= maxFps) {
                durationsMs.mapIndexed { index, duration -> SampledFrame(index, duration) }
            } else {
                resample(durationsMs, maxFps)
            }
        return enforceMinimumDuration(raw)
    }

    private fun resample(
        durationsMs: List<Long>,
        maxFps: Int,
    ): List<SampledFrame> {
        val total = durationsMs.sum()
        val interval = MS_PER_SECOND / maxFps
        val outputFrames = ceil(total / interval).toInt()
        val starts = (0 until outputFrames).map { (it * interval).roundToLong() }.filter { it < total }
        val frameEnds = durationsMs.runningReduce(Long::plus)
        val sampled =
            starts.mapIndexed { k, start ->
                val end = starts.getOrElse(k + 1) { total }
                SampledFrame(sourceIndex = frameEnds.indexOfFirst { it > start }, durationMs = end - start)
            }
        return mergeRepeatedFrames(sampled)
    }

    private fun mergeRepeatedFrames(frames: List<SampledFrame>): List<SampledFrame> =
        frames.fold(mutableListOf()) { merged, frame ->
            val last = merged.lastOrNull()
            if (last != null && last.sourceIndex == frame.sourceIndex) {
                merged[merged.lastIndex] = last.copy(durationMs = last.durationMs + frame.durationMs)
            } else {
                merged += frame
            }
            merged
        }

    /** A too-short frame lends its time to the next frame, or to the previous one when it is last. */
    private fun enforceMinimumDuration(frames: List<SampledFrame>): List<SampledFrame> {
        val result = mutableListOf<SampledFrame>()
        var carried = 0L
        frames.forEachIndexed { index, frame ->
            val duration = carried + frame.durationMs
            carried = 0
            when {
                duration >= StickerLimits.ANIMATED_MIN_FRAME_MS -> result += frame.copy(durationMs = duration)
                index < frames.lastIndex -> carried = duration
                result.isNotEmpty() ->
                    result[result.lastIndex] =
                        result.last().let { it.copy(durationMs = it.durationMs + duration) }
                else -> result += frame.copy(durationMs = duration)
            }
        }
        return result
    }
}
