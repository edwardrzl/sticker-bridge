package com.edrl.stickerbridge.core.pack

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PackPaddingTest {
    @Test
    fun `a pack with one sticker is offered with two copies of it`() {
        val offered = PackPadding.offered(pack(stickers = 1))

        assertEquals(listOf("s0.webp", "copy1_s0.webp", "copy2_s0.webp"), offered.map { it.fileName })
        assertTrue(offered.all { it.sticker.id == "s0" })
    }

    @Test
    fun `a pack with two stickers is offered with one copy of the first`() {
        val offered = PackPadding.offered(pack(stickers = 2))

        assertEquals(listOf("s0.webp", "s1.webp", "copy1_s0.webp"), offered.map { it.fileName })
        assertEquals(listOf("s0", "s1", "s0"), offered.map { it.sticker.id })
    }

    @Test
    fun `a pack with three or more stickers is offered as it is`() {
        assertEquals(
            listOf("s0.webp", "s1.webp", "s2.webp"),
            PackPadding.offered(pack(stickers = 3)).map { it.fileName },
        )
        assertEquals(30, PackPadding.offered(pack(stickers = 30)).size)
    }

    @Test
    fun `an empty pack offers nothing`() {
        assertTrue(PackPadding.offered(pack(stickers = 0)).isEmpty())
    }

    @Test
    fun `copies keep the file extension WhatsApp requires`() {
        assertTrue(PackPadding.offered(pack(stickers = 1)).all { it.fileName.endsWith(".webp") })
    }
}
