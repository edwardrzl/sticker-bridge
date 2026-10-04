package com.edrl.stickerbridge.conversion

import android.graphics.Bitmap
import com.edrl.stickerbridge.core.conversion.DecodedImage
import com.edrl.stickerbridge.core.conversion.SourceImageInfo

/** A decoded image whose frames the Android encoder can draw. */
sealed class AndroidDecodedImage : DecodedImage {
    abstract fun frame(index: Int): Bitmap
}

/** A still image. */
class BitmapDecodedImage(
    private val bitmap: Bitmap,
) : AndroidDecodedImage() {
    override val info = SourceImageInfo(bitmap.width, bitmap.height, frameDurationsMs = listOf(0L))

    override fun frame(index: Int): Bitmap = bitmap

    override fun close() = bitmap.recycle()
}

/** The frames of an animation, already scaled to their place on the sticker canvas. */
class AnimatedDecodedImage(
    private val frames: List<Bitmap>,
    frameDurationsMs: List<Long>,
) : AndroidDecodedImage() {
    init {
        require(frames.isNotEmpty() && frames.size == frameDurationsMs.size) { "one duration per frame" }
    }

    override val info = SourceImageInfo(frames.first().width, frames.first().height, frameDurationsMs)

    override fun frame(index: Int): Bitmap = frames[index]

    override fun close() = frames.forEach(Bitmap::recycle)
}
