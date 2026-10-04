package com.edrl.stickerbridge.conversion

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.aureusapps.android.webpandroid.CodecException
import com.aureusapps.android.webpandroid.CodecResult
import com.aureusapps.android.webpandroid.decoder.WebPDecoder
import com.edrl.stickerbridge.core.conversion.FitCalculator
import com.edrl.stickerbridge.core.conversion.FrameBudget
import com.edrl.stickerbridge.core.conversion.Placement
import com.edrl.stickerbridge.core.diagnostics.DiagnosticLog
import java.io.File
import java.util.UUID

/**
 * Decodes the frames of an animated WebP with libwebp (ADR-003). Frames are scaled to their place
 * on the sticker canvas as they are decoded and kept within [FrameBudget].
 */
class AnimatedWebpDecoder(
    private val context: Context,
    private val workDir: File,
    private val log: DiagnosticLog,
) {
    /** The decoded animation, or null when libwebp cannot read it. */
    fun decode(bytes: ByteArray): AnimatedDecodedImage? {
        workDir.mkdirs()
        val source = File(workDir, "${UUID.randomUUID()}.webp").apply { writeBytes(bytes) }
        val decoder = WebPDecoder(context)
        return try {
            decoder.setDataSource(Uri.fromFile(source))
            decodeFrames(decoder)
        } catch (e: CodecException) {
            log.event(TAG, "animated webp could not be decoded", e)
            null
        } finally {
            decoder.release()
            source.delete()
        }
    }

    private fun decodeFrames(decoder: WebPDecoder): AnimatedDecodedImage? {
        val info = decoder.decodeInfo()
        val placement = FitCalculator.fit(info.width, info.height)
        val keepEvery = FrameBudget.keepEvery(placement.width, placement.height, info.frameCount)
        val frames = mutableListOf<Bitmap>()
        val durations = mutableListOf<Long>()
        var previousEnd = 0L
        var index = 0
        while (decoder.hasNextFrame()) {
            val result = decoder.decodeNextFrame()
            val frame = result.frame
            if (result.codecResult != CodecResult.SUCCESS || frame == null) {
                log.event(TAG, "frame $index failed: ${result.codecResult}")
                frames.forEach(Bitmap::recycle)
                return null
            }
            // libwebp reports when each frame ends; its duration is the gap to the previous end.
            val end = result.timestamp.toLong()
            val duration = (end - previousEnd).coerceAtLeast(0)
            previousEnd = end
            if (index % keepEvery == 0) {
                frames += scaledCopy(frame, placement)
                durations += duration
            } else {
                durations[durations.lastIndex] += duration
            }
            index++
        }
        log.event(TAG, "decoded ${frames.size}/$index frames, ${durations.sum()} ms, keep 1 in $keepEvery")
        return if (frames.isEmpty()) null else AnimatedDecodedImage(frames, durations)
    }

    /** The decoder reuses its bitmap, so every kept frame is copied. */
    private fun scaledCopy(
        frame: Bitmap,
        placement: Placement,
    ): Bitmap {
        val scaled = Bitmap.createScaledBitmap(frame, placement.width, placement.height, true)
        return if (scaled === frame) frame.copy(Bitmap.Config.ARGB_8888, false) else scaled
    }

    private companion object {
        const val TAG = "conversion"
    }
}
