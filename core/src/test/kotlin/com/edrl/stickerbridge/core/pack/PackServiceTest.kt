package com.edrl.stickerbridge.core.pack

import com.edrl.stickerbridge.core.conversion.ConvertedSticker
import com.edrl.stickerbridge.core.conversion.EncodedFile
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PackServiceTest {
    private val repository = FakePackRepository()
    private var nextId = 0
    private val service =
        PackService(
            repository = repository,
            trayIcons = { sticker -> EncodedFile("tray-from-${sticker.path}", 2_000) },
            ids = { "id${nextId++}" },
        )

    private fun static(path: String) =
        ConvertedSticker(EncodedFile(path, 40_000), PackSeries.STATIC, animationDropped = false)

    @Test
    fun `first static sticker creates the first static pack with fixed naming`() =
        runTest {
            val pack = service.addSticker(static("a.webp"))

            assertEquals("tiktok_static_1", pack.identifier)
            assertEquals("TikTok estáticos 1", pack.name)
            assertEquals("TikTok Stickers", pack.publisher)
            assertEquals(1, pack.number)
            assertEquals(PackSeries.STATIC, pack.series)
        }

    @Test
    fun `every sticker gets the default emoji and a webp file name from its id`() =
        runTest {
            val pack = service.addSticker(static("a.webp"))

            val sticker = pack.stickers.single()
            assertEquals("😀", sticker.emoji)
            assertEquals("id0.webp", sticker.fileName)
            assertEquals(40_000L, sticker.sizeBytes)
        }

    @Test
    fun `a new pack stages the sticker file and a tray icon`() =
        runTest {
            service.addSticker(static("a.webp"))

            val staged = repository.lastStaged
            assertEquals(
                listOf(
                    StagedFile("a.webp", "tiktok_static_1", "id0.webp"),
                    StagedFile("tray-from-a.webp", "tiktok_static_1", StickerPack.TRAY_ICON_FILE),
                ),
                staged,
            )
        }

    @Test
    fun `adding to an existing pack appends and increases the image data version`() =
        runTest {
            service.addSticker(static("a.webp"))
            val pack = service.addSticker(static("b.webp"))

            assertEquals(listOf("id0.webp", "id1.webp"), pack.stickers.map { it.fileName })
            assertEquals(2, pack.imageDataVersion)
            assertEquals(listOf(StagedFile("b.webp", "tiktok_static_1", "id1.webp")), repository.lastStaged)
        }

    @Test
    fun `animated stickers go to their own series`() =
        runTest {
            service.addSticker(static("a.webp"))
            val animated =
                service.addSticker(ConvertedSticker(EncodedFile("m.webp", 300_000), PackSeries.ANIMATED, false))

            assertEquals("tiktok_animated_1", animated.identifier)
            assertEquals("TikTok animados 1", animated.name)
            assertEquals(2, repository.packs.size)
        }

    @Test
    fun `a failed commit leaves the stored packs unchanged`() =
        runTest {
            service.addSticker(static("a.webp"))
            repository.failNextCommit = true

            assertFailsWith<StorageException> { service.addSticker(static("b.webp")) }

            assertEquals(
                1,
                service
                    .listPacks()
                    .single()
                    .stickers.size,
            )
        }

    @Test
    fun `status depends on sticker count and whatsapp`() {
        val two = pack(stickers = 2)
        val three = pack(stickers = 3)

        assertEquals(PackStatus.NeedsMore(1), PackStatus.of(two, addedToWhatsApp = false))
        assertEquals(PackStatus.ReadyToAdd, PackStatus.of(three, addedToWhatsApp = false))
        assertEquals(PackStatus.Added, PackStatus.of(three, addedToWhatsApp = true))
        assertTrue(PackStatus.of(two, addedToWhatsApp = true) is PackStatus.NeedsMore)
    }
}

internal fun pack(
    stickers: Int,
    series: PackSeries = PackSeries.STATIC,
): StickerPack =
    StickerPack(
        identifier = PackNaming.identifier(series, 1),
        name = PackNaming.name(series, 1),
        publisher = PackNaming.PUBLISHER,
        series = series,
        number = 1,
        imageDataVersion = 1,
        stickers = List(stickers) { Sticker("s$it", "s$it.webp", PackNaming.DEFAULT_EMOJI, 40_000) },
    )

internal class FakePackRepository : PackRepository {
    var packs: List<StickerPack> = emptyList()
    var lastStaged: List<StagedFile> = emptyList()
    var failNextCommit = false

    override suspend fun load(): List<StickerPack> = packs

    override suspend fun commit(
        packs: List<StickerPack>,
        staged: List<StagedFile>,
    ) {
        if (failNextCommit) {
            failNextCommit = false
            throw StorageException("disk full")
        }
        this.packs = packs
        lastStaged = staged
    }
}
