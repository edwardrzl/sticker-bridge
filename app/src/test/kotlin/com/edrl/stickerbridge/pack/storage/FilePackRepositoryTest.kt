package com.edrl.stickerbridge.pack.storage

import com.edrl.stickerbridge.core.pack.FileSource
import com.edrl.stickerbridge.core.pack.PackFile
import com.edrl.stickerbridge.core.pack.PackNaming
import com.edrl.stickerbridge.core.pack.PackSeries
import com.edrl.stickerbridge.core.pack.StagedFile
import com.edrl.stickerbridge.core.pack.Sticker
import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.core.pack.StorageException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FilePackRepositoryTest {
    @TempDir
    lateinit var root: File

    private fun pack(number: Int) =
        StickerPack(
            identifier = PackNaming.identifier(PackSeries.STATIC, number),
            name = PackNaming.name(PackSeries.STATIC, number),
            publisher = PackNaming.PUBLISHER,
            series = PackSeries.STATIC,
            number = number,
            imageDataVersion = 1,
            stickers = listOf(Sticker("a", "a.webp", PackNaming.DEFAULT_EMOJI, 4)),
        )

    private val first = pack(1)
    private val second = pack(2)

    private fun temp(content: String): FileSource.Temp =
        FileSource.Temp(File(root, "staged-$content").apply { writeText(content) }.path)

    @Test
    fun `an empty store has no packs`() =
        runTest {
            assertTrue(FilePackRepository(root).load().isEmpty())
        }

    @Test
    fun `commit moves temporary files into the pack folder and stores the index`() =
        runTest {
            val repository = FilePackRepository(root)
            val source = temp("webp")

            repository.commit(listOf(first), listOf(StagedFile(source, first.identifier, "a.webp")))

            assertEquals(listOf(first), FilePackRepository(root).load())
            assertEquals("webp", repository.fileOf(first.identifier, "a.webp")!!.readText())
            assertFalse(File(source.path).exists())
        }

    @Test
    fun `files from another pack are copied and the originals removed only after the index`() =
        runTest {
            val repository = FilePackRepository(root)
            repository.commit(listOf(first), listOf(StagedFile(temp("moving"), first.identifier, "a.webp")))

            repository.commit(
                listOf(first, second),
                listOf(StagedFile(FileSource.InPack(first.identifier, "a.webp"), second.identifier, "a.webp")),
                obsolete = listOf(PackFile(first.identifier, "a.webp")),
            )

            assertEquals("moving", repository.fileOf(second.identifier, "a.webp")!!.readText())
            assertFalse(repository.fileOf(first.identifier, "a.webp")!!.exists())
        }

    @Test
    fun `a failure before the index keeps the previous state and deletes nothing`() =
        runTest {
            val repository = FilePackRepository(root)
            repository.commit(listOf(first), listOf(StagedFile(temp("keep"), first.identifier, "a.webp")))
            val missing = StagedFile(FileSource.Temp(File(root, "does-not-exist").path), first.identifier, "b.webp")

            assertFailsWith<StorageException> {
                repository.commit(emptyList(), listOf(missing), obsolete = listOf(PackFile(first.identifier, "a.webp")))
            }

            assertEquals(listOf(first), repository.load())
            assertTrue(repository.fileOf(first.identifier, "a.webp")!!.exists())
        }

    @Test
    fun `a leftover temporary index is ignored`() =
        runTest {
            val repository = FilePackRepository(root)
            repository.commit(listOf(first), emptyList())
            File(root, "packs/packs.json.tmp").writeText("garbage")

            assertEquals(listOf(first), repository.load())
        }

    @Test
    fun `names that would leave the packs folder are rejected`() {
        val repository = FilePackRepository(root)

        assertNull(repository.fileOf("..", "a.webp"))
        assertNull(repository.fileOf(first.identifier, "../packs.json"))
    }
}
