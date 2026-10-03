package com.edrl.stickerbridge.core.conversion

/**
 * Fits an image into the square sticker canvas (BR-06): scaled up or down until its longest side
 * is [CANVAS] pixels, proportions kept, centered. The rest of the canvas stays transparent.
 */
object FitCalculator {
    const val CANVAS = 512

    fun fit(
        width: Int,
        height: Int,
    ): Placement {
        require(width > 0 && height > 0) { "image size must be positive, was ${width}x$height" }
        val longest = maxOf(width, height)
        val fittedWidth = scale(width, longest)
        val fittedHeight = scale(height, longest)
        return Placement(
            width = fittedWidth,
            height = fittedHeight,
            offsetX = (CANVAS - fittedWidth) / 2,
            offsetY = (CANVAS - fittedHeight) / 2,
        )
    }

    private fun scale(
        side: Int,
        longest: Int,
    ): Int = ((side.toLong() * CANVAS + longest / 2) / longest).toInt().coerceIn(1, CANVAS)
}
