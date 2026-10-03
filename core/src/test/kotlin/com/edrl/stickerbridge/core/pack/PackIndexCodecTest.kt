package com.edrl.stickerbridge.core.pack

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PackIndexCodecTest {
    @Test
    fun `round trips packs through json`() {
        val packs = listOf(pack(stickers = 3), pack(stickers = 1, series = PackSeries.ANIMATED))

        assertEquals(packs, PackIndexCodec.decode(PackIndexCodec.encode(packs)))
    }

    @Test
    fun `writes the schema version`() {
        assertTrue(PackIndexCodec.encode(emptyList()).contains("\"schemaVersion\": 1"))
    }

    @Test
    fun `rejects an unknown schema version`() {
        assertFailsWith<StorageException> { PackIndexCodec.decode("""{"schemaVersion":99,"packs":[]}""") }
    }

    @Test
    fun `rejects corrupted content`() {
        assertFailsWith<StorageException> { PackIndexCodec.decode("{not json") }
    }
}
