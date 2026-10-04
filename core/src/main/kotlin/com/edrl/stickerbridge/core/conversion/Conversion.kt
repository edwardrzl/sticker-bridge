package com.edrl.stickerbridge.core.conversion

import com.edrl.stickerbridge.core.pack.PackSeries

/** Where an image to convert comes from. */
sealed interface ImageRef {
    data class Remote(
        val url: String,
    ) : ImageRef

    /** An image picked from the phone's gallery, identified by its content URI. */
    data class Local(
        val uri: String,
    ) : ImageRef
}

data class SourceImageInfo(
    val width: Int,
    val height: Int,
    /** One entry per frame; a still image has a single frame of duration 0. */
    val frameDurationsMs: List<Long>,
) {
    val frameCount: Int get() = frameDurationsMs.size
    val totalDurationMs: Long get() = frameDurationsMs.sum()
    val isAnimated: Boolean get() = frameCount > 1
}

/** One frame of an output animation: which source frame it shows and for how long. */
data class SampledFrame(
    val sourceIndex: Int,
    val durationMs: Long,
)

/** A decoded image held by an adapter. Core only sees its description. */
interface DecodedImage : AutoCloseable {
    val info: SourceImageInfo
}

/** Size and position of the image inside the 512×512 sticker canvas. */
data class Placement(
    val width: Int,
    val height: Int,
    val offsetX: Int,
    val offsetY: Int,
)

/** An encoded file written by an adapter, identified by its path. */
data class EncodedFile(
    val path: String,
    val sizeBytes: Long,
)

sealed interface ConversionError {
    data object DownloadFailed : ConversionError

    data object UnsupportedImage : ConversionError

    /** No encoding attempt fits WhatsApp's size limit. */
    data object TooLarge : ConversionError
}

sealed interface ImageOutcome {
    data class Opened(
        val image: DecodedImage,
    ) : ImageOutcome

    data class Failed(
        val error: ConversionError,
    ) : ImageOutcome
}

/** A sticker ready to be added to a pack of [series]. */
data class ConvertedSticker(
    val file: EncodedFile,
    val series: PackSeries,
    val animationDropped: Boolean,
)

sealed interface ConversionResult {
    data class Converted(
        val sticker: ConvertedSticker,
    ) : ConversionResult

    data class Failed(
        val error: ConversionError,
    ) : ConversionResult
}

/** Port: downloads or reads an image and decodes it. */
fun interface ImageSource {
    suspend fun open(ref: ImageRef): ImageOutcome
}

/** Port: encodes decoded images as WebP sticker files. */
interface StickerEncoder {
    suspend fun encodeStatic(
        image: DecodedImage,
        placement: Placement,
        quality: Int,
    ): EncodedFile

    /** Encodes an animated sticker showing [frames] of [image], in order. */
    suspend fun encodeAnimated(
        image: DecodedImage,
        placement: Placement,
        frames: List<SampledFrame>,
        quality: Int,
    ): EncodedFile

    /** Deletes a file produced by this encoder that will not be used. */
    fun discard(file: EncodedFile)
}
