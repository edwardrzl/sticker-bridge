package com.edrl.stickerbridge.core.conversion

/**
 * Lowers the compression quality step by step until a static sticker fits the size limit (BR-07).
 */
object StaticQualityLadder {
    private const val HIGHEST_QUALITY = 90
    private const val LOWEST_QUALITY = 10
    private const val QUALITY_STEP = 10

    val QUALITIES: List<Int> = (HIGHEST_QUALITY downTo LOWEST_QUALITY step QUALITY_STEP).toList()

    /**
     * Returns the first attempt that weighs at most [limitBytes], or null when none does.
     * Every rejected attempt is passed to [discard].
     */
    suspend fun encode(
        limitBytes: Long,
        discard: (EncodedFile) -> Unit,
        attempt: suspend (quality: Int) -> EncodedFile,
    ): EncodedFile? {
        for (quality in QUALITIES) {
            val file = attempt(quality)
            if (file.sizeBytes <= limitBytes) return file
            discard(file)
        }
        return null
    }
}
