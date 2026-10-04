package com.edrl.stickerbridge.core.conversion

/** Recognises animated WebP files from their first bytes (RIFF container with a VP8X header). */
object WebpSniffer {
    private const val RIFF_OFFSET = 0
    private const val WEBP_OFFSET = 8
    private const val CHUNK_OFFSET = 12
    private const val FLAGS_OFFSET = 20
    private const val TAG_LENGTH = 4
    private const val ANIMATION_FLAG = 0x02

    fun isAnimatedWebp(bytes: ByteArray): Boolean =
        bytes.size > FLAGS_OFFSET &&
            bytes.tagAt(RIFF_OFFSET) == "RIFF" &&
            bytes.tagAt(WEBP_OFFSET) == "WEBP" &&
            bytes.tagAt(CHUNK_OFFSET) == "VP8X" &&
            bytes[FLAGS_OFFSET].toInt() and ANIMATION_FLAG != 0

    private fun ByteArray.tagAt(offset: Int): String = String(this, offset, TAG_LENGTH, Charsets.US_ASCII)
}
