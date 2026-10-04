package com.edrl.stickerbridge.core.conversion

import com.edrl.stickerbridge.core.pack.PackSeries
import com.edrl.stickerbridge.core.pack.StickerLimits

/**
 * Converts one image into a WhatsApp sticker without any manual step (FR4).
 *
 * A short animation stays animated if some attempt of [AnimatedQualityLadder] fits the size
 * limit; otherwise, and for animations longer than ten seconds, the first frame becomes a static
 * sticker and the result says the animation was dropped (BR-08 to BR-11).
 */
class StickerConverter(
    private val source: ImageSource,
    private val encoder: StickerEncoder,
) {
    suspend fun convert(ref: ImageRef): ConversionResult {
        val image =
            when (val opened = source.open(ref)) {
                is ImageOutcome.Opened -> opened.image
                is ImageOutcome.Failed -> return ConversionResult.Failed(opened.error)
            }
        return image.use { decoded ->
            val placement = FitCalculator.fit(decoded.info.width, decoded.info.height)
            val animated = if (isAnimationCandidate(decoded.info)) encodeAnimated(decoded, placement) else null
            if (animated != null) {
                ConversionResult.Converted(ConvertedSticker(animated, PackSeries.ANIMATED, animationDropped = false))
            } else {
                encodeStatic(decoded, placement, animationDropped = decoded.info.isAnimated)
            }
        }
    }

    private fun isAnimationCandidate(info: SourceImageInfo): Boolean =
        info.isAnimated && info.totalDurationMs <= StickerLimits.ANIMATED_MAX_DURATION_MS

    private suspend fun encodeAnimated(
        image: DecodedImage,
        placement: Placement,
    ): EncodedFile? {
        val durations = image.info.frameDurationsMs
        for (attempt in AnimatedQualityLadder.attemptsFor(durations)) {
            val frames = FrameSampler.sample(durations, attempt.maxFps)
            val file = encoder.encodeAnimated(image, placement, frames, attempt.quality)
            if (file.sizeBytes <= StickerLimits.ANIMATED_MAX_BYTES) return file
            encoder.discard(file)
        }
        return null
    }

    private suspend fun encodeStatic(
        image: DecodedImage,
        placement: Placement,
        animationDropped: Boolean,
    ): ConversionResult {
        val file =
            StaticQualityLadder.encode(StickerLimits.STATIC_MAX_BYTES, encoder::discard) { quality ->
                encoder.encodeStatic(image, placement, quality)
            }
        return if (file == null) {
            ConversionResult.Failed(ConversionError.TooLarge)
        } else {
            ConversionResult.Converted(ConvertedSticker(file, PackSeries.STATIC, animationDropped))
        }
    }
}
