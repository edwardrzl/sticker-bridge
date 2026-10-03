package com.edrl.stickerbridge.core.conversion

import com.edrl.stickerbridge.core.pack.PackSeries
import com.edrl.stickerbridge.core.pack.StickerLimits

/**
 * Converts one image into a WhatsApp sticker without any manual step.
 *
 * In the walking skeleton every image becomes a static sticker from its first frame; animated
 * output is added in unit U3.
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
            val file =
                StaticQualityLadder.encode(StickerLimits.STATIC_MAX_BYTES, encoder::discard) { quality ->
                    encoder.encodeStatic(decoded, placement, quality)
                }
            if (file == null) {
                ConversionResult.Failed(ConversionError.TooLarge)
            } else {
                ConversionResult.Converted(
                    ConvertedSticker(file, PackSeries.STATIC, animationDropped = decoded.info.frameCount > 1),
                )
            }
        }
    }
}
