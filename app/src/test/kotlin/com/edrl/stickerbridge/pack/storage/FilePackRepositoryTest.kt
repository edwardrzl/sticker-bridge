package com.edrl.stickerbridge.pack.storage

import com.edrl.stickerbridge.core.pack.PackNaming
import com.edrl.stickerbridge.core.pack.PackSeries
import com.edrl.stickerbridge.core.pack.StagedFile
import com.edrl.stickerbridge.core.pack.Sticker
import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.core.pack.StorageException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FilePackRepositoryTest {
    @TempDir
    lateinit var root: File

    private val pack =
        StickerPack(
            identifier = PackNaming.identifier(PackSeries.STATIC, 1),
            name = PackNaming.name(PackSeries.STATIC, 1),
            publisher = PackNaming.PUBLISHER,
            series = PackSeries.STATIC,
            number = 1,
            imageDataVersion = 1,
            stickers = listOf(Sticker("a", "a.webp", PackNaming.DEFAULT_EMOJI, 4)),
        )

    private fun stagedSource(content: String): File = File(root, "staged-$content").apply { writeText(content) }

    @Test
    fun `an empty store has no packs`() =
        runTest {
            assertTrue(FilePackRepository(root).load().isEmpty())
        }

    @Test
    fun `commit moves staged files into the pack folder and stores the index`() =
        runTest {
            val repository = FilePackRepository(root)
            val source = stagedSource("webp")

            repository.commit(listOf(pack), listOf(StagedFile(source.path, pack.identifier, "a.webp")))

            assertEquals(listOf(pack), FilePackRepository(root).load())
            assertEquals("webp", repository.fileOf(pack.identifier, "a.webp")!!.readText())
            assertTrue(!source.exists())
        }

    @Test
    fun `a leftover temporary index is ignored`() =
        runTest {
            val repository = FilePackRepository(root)
            repository.commit(listOf(pack), emptyList())
            File(root, "packs/packs.json.tmp").writeText("garbage")

            assertEquals(listOf(pack), repository.load())
        }

    @Test
    fun `a failed file move keeps the previous index`() =
        runTest {
            val repository = FilePackRepository(root)
            repository.commit(listOf(pack), emptyList())
            val missing = StagedFile(File(root, "does-not-exist").path, pack.identifier, "b.webp")

            assertFailsWith<StorageException> { repository.commit(emptyList(), listOf(missing)) }

            assertEquals(listOf(pack), repository.load())
        }

    @Test
    fun `names that would leave the packs folder are rejected`() {
        val repository = FilePackRepository(root)

        assertNull(repository.fileOf("..", "a.webp"))
        assertNull(repository.fileOf(pack.identifier, "../packs.json"))
    }
}
