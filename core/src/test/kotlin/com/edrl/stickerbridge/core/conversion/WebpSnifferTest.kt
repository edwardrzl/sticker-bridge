package com.edrl.stickerbridge.core.conversion

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebpSnifferTest {
    private fun webp(
        chunk: String,
        flags: Int = 0,
    ): ByteArray {
        val header = "RIFF".toByteArray() + ByteArray(4) + "WEBP".toByteArray() + chunk.toByteArray()
        return header + ByteArray(4) + byteArrayOf(flags.toByte()) + ByteArray(9)
    }

    @Test
    fun `recognises an animated webp by its extended header flag`() {
        assertTrue(WebpSniffer.isAnimatedWebp(webp("VP8X", flags = 0x02)))
        assertTrue(WebpSniffer.isAnimatedWebp(webp("VP8X", flags = 0x12)))
    }

    @Test
    fun `an extended webp without the animation flag is static`() {
        assertFalse(WebpSniffer.isAnimatedWebp(webp("VP8X", flags = 0x10)))
    }

    @Test
    fun `simple webp, jpeg and short data are not animated`() {
        assertFalse(WebpSniffer.isAnimatedWebp(webp("VP8 ")))
        assertFalse(
            WebpSniffer.isAnimatedWebp(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()) + ByteArray(30)),
        )
        assertFalse(WebpSniffer.isAnimatedWebp(ByteArray(5)))
    }
}
