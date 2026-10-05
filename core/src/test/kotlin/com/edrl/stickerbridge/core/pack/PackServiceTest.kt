package com.edrl.stickerbridge.core.pack

import com.edrl.stickerbridge.core.conversion.ConvertedSticker
import com.edrl.stickerbridge.core.conversion.EncodedFile
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PackServiceTest {
    private val repository = FakePackRepository()
    private var nextId = 0
    private val renderedTrays = mutableListOf<FileSource>()
    private val service =
        PackService(
            repository = repository,
            trayIcons = { source ->
                renderedTrays += source
                EncodedFile("tray-${renderedTrays.size}.png", 2_000)
            },
            ids = { "id${nextId++}" },
        )

    private fun static(path: String) =
        ConvertedSticker(EncodedFile(path, 40_000), PackSeries.STATIC, animationDropped = false)

    private suspend fun fill(count: Int) = repeat(count) { service.addSticker(static("s$it.webp")) }

    @Test
    fun `first static sticker creates the first static pack with fixed naming`() =
        runTest {
            val pack = service.addSticker(static("a.webp")).target

            assertEquals("tiktok_static_1", pack.identifier)
            assertEquals("TikTok estáticos 1", pack.name)
            assertEquals("TikTok Stickers", pack.publisher)
            assertEquals(1, pack.number)
            assertEquals(PackSeries.STATIC, pack.series)
        }

    @Test
    fun `every sticker gets the default emoji and a webp file name from its id`() =
        runTest {
            val sticker =
                service
                    .addSticker(static("a.webp"))
                    .target.stickers
                    .single()

            assertEquals("😀", sticker.emoji)
            assertEquals("id0.webp", sticker.fileName)
            assertEquals(40_000L, sticker.sizeBytes)
        }

    @Test
    fun `a new pack stages the sticker file and a tray icon rendered from it`() =
        runTest {
            service.addSticker(static("a.webp"))

            assertEquals(listOf<FileSource>(FileSource.Temp("a.webp")), renderedTrays)
            assertEquals(
                listOf(
                    StagedFile(FileSource.Temp("a.webp"), "tiktok_static_1", "id0.webp"),
                    StagedFile(FileSource.Temp("tray-1.png"), "tiktok_static_1", StickerPack.TRAY_ICON_FILE),
                ),
                repository.lastStaged,
            )
        }

    @Test
    fun `adding to an existing pack appends and increases the image data version`() =
        runTest {
            service.addSticker(static("a.webp"))
            val pack = service.addSticker(static("b.webp")).target

            assertEquals(listOf("id0.webp", "id1.webp"), pack.stickers.map { it.fileName })
            assertEquals(2, pack.imageDataVersion)
            assertEquals(
                listOf(StagedFile(FileSource.Temp("b.webp"), "tiktok_static_1", "id1.webp")),
                repository.lastStaged,
            )
        }

    @Test
    fun `animated stickers go to their own series`() =
        runTest {
            service.addSticker(static("a.webp"))
            val animated =
                service.addSticker(ConvertedSticker(EncodedFile("m.webp", 300_000), PackSeries.ANIMATED, false)).target

            assertEquals("tiktok_animated_1", animated.identifier)
            assertEquals("TikTok animados 1", animated.name)
            assertEquals(2, repository.packs.size)
        }

    @Test
    fun `the 31st sticker opens the next pack with the last two of the full one`() =
        runTest {
            fill(30)

            val result = service.addSticker(static("new.webp"))

            val (full, next) = repository.packs.sortedBy { it.number }
            assertEquals(28, full.stickers.size)
            assertEquals(listOf("id28.webp", "id29.webp", "id30.webp"), next.stickers.map { it.fileName })
            assertEquals("tiktok_static_2", result.target.identifier)
            assertEquals(setOf(full.identifier, next.identifier), result.changed.map { it.identifier }.toSet())
            assertEquals(31, (full.stickers + next.stickers).map { it.id }.toSet().size)
        }

    @Test
    fun `splitting copies the moved files, renders the new tray and retires the originals`() =
        runTest {
            fill(30)
            renderedTrays.clear()

            service.addSticker(static("new.webp"))

            assertEquals(listOf<FileSource>(FileSource.InPack("tiktok_static_1", "id28.webp")), renderedTrays)
            assertTrue(
                StagedFile(FileSource.InPack("tiktok_static_1", "id28.webp"), "tiktok_static_2", "id28.webp") in
                    repository.lastStaged,
            )
            assertEquals(
                listOf(PackFile("tiktok_static_1", "id28.webp"), PackFile("tiktok_static_1", "id29.webp")),
                repository.lastObsolete,
            )
        }

    @Test
    fun `splitting increases the version of the full pack`() =
        runTest {
            fill(30)

            service.addSticker(static("new.webp"))

            assertEquals(31, repository.packs.first { it.number == 1 }.imageDataVersion)
        }

    @Test
    fun `removing a sticker drops it, bumps the version and retires its file`() =
        runTest {
            fill(4)

            val pack = service.removeSticker("tiktok_static_1", "id2")!!

            assertEquals(listOf("id0", "id1", "id3"), pack.stickers.map { it.id })
            assertEquals(5, pack.imageDataVersion)
            assertEquals(listOf(PackFile("tiktok_static_1", "id2.webp")), repository.lastObsolete)
        }

    @Test
    fun `removing the first sticker renders the tray from the new first one`() =
        runTest {
            fill(3)
            renderedTrays.clear()

            service.removeSticker("tiktok_static_1", "id0")

            assertEquals(listOf<FileSource>(FileSource.InPack("tiktok_static_1", "id1.webp")), renderedTrays)
            assertTrue(repository.lastStaged.any { it.fileName == StickerPack.TRAY_ICON_FILE })
        }

    @Test
    fun `removing the last sticker deletes the pack`() =
        runTest {
            fill(1)

            assertNull(service.removeSticker("tiktok_static_1", "id0"))
            assertTrue(repository.packs.isEmpty())
            assertTrue(PackFile("tiktok_static_1", StickerPack.TRAY_ICON_FILE) in repository.lastObsolete)
        }

    @Test
    fun `removing an unknown sticker changes nothing`() =
        runTest {
            fill(3)
            val before = repository.packs

            service.removeSticker("tiktok_static_1", "nope")

            assertEquals(before, repository.packs)
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
    fun `status depends only on whatsapp, whatever the sticker count`() {
        val one = pack(stickers = 1)
        val three = pack(stickers = 3)

        assertEquals(PackStatus.ReadyToAdd, PackStatus.of(one, addedToWhatsApp = false))
        assertEquals(PackStatus.Added, PackStatus.of(one, addedToWhatsApp = true))
        assertEquals(PackStatus.ReadyToAdd, PackStatus.of(three, addedToWhatsApp = false))
        assertEquals(PackStatus.Added, PackStatus.of(three, addedToWhatsApp = true))
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
    var lastObsolete: List<PackFile> = emptyList()
    var failNextCommit = false

    override suspend fun load(): List<StickerPack> = packs

    override suspend fun commit(
        packs: List<StickerPack>,
        staged: List<StagedFile>,
        obsolete: List<PackFile>,
    ) {
        if (failNextCommit) {
            failNextCommit = false
            throw StorageException("disk full")
        }
        this.packs = packs
        lastStaged = staged
        lastObsolete = obsolete
    }
}
