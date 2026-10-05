package com.edrl.stickerbridge.core.pack

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PackValidatorTest {
    @Test
    fun `a pack with three valid stickers is valid`() {
        assertTrue(PackValidator.validate(pack(stickers = 3)).isEmpty())
    }

    @Test
    fun `a pack with one sticker is valid because it is padded when offered`() {
        assertTrue(PackValidator.validate(pack(stickers = 1)).isEmpty())
    }

    @Test
    fun `an empty pack is not valid`() {
        assertEquals(listOf(PackViolation.TooFewStickers(0)), PackValidator.validate(pack(stickers = 0)))
    }

    @Test
    fun `more than thirty stickers is too many`() {
        assertEquals(listOf(PackViolation.TooManyStickers(31)), PackValidator.validate(pack(stickers = 31)))
    }

    @Test
    fun `static stickers over 100 KB are rejected`() {
        val heavy = pack(stickers = 3).let { it.copy(stickers = it.stickers.map { s -> s.copy(sizeBytes = 100_001) }) }

        assertEquals(3, PackValidator.validate(heavy).filterIsInstance<PackViolation.StickerTooLarge>().size)
    }

    @Test
    fun `animated stickers may weigh up to 500 KB`() {
        val animated =
            pack(stickers = 3, series = PackSeries.ANIMATED).let {
                it.copy(stickers = it.stickers.map { s -> s.copy(sizeBytes = 500_000) })
            }

        assertTrue(PackValidator.validate(animated).isEmpty())
    }

    @Test
    fun `identifier with forbidden characters is rejected`() {
        val bad = pack(stickers = 3).copy(identifier = "bad/id")

        assertEquals(listOf(PackViolation.InvalidIdentifier("bad/id")), PackValidator.validate(bad))
    }

    @Test
    fun `stickers need between one and three emojis`() {
        val noEmoji = pack(stickers = 3).let { it.copy(stickers = it.stickers.map { s -> s.copy(emoji = "") }) }

        assertEquals(3, PackValidator.validate(noEmoji).filterIsInstance<PackViolation.MissingEmoji>().size)
    }
}
