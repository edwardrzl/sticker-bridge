package com.edrl.stickerbridge.core.save

import com.edrl.stickerbridge.core.conversion.ConversionError
import com.edrl.stickerbridge.core.conversion.ConversionResult
import com.edrl.stickerbridge.core.conversion.ConvertedSticker
import com.edrl.stickerbridge.core.conversion.EncodedFile
import com.edrl.stickerbridge.core.conversion.ImageConverter
import com.edrl.stickerbridge.core.conversion.ImageRef
import com.edrl.stickerbridge.core.pack.FakePackRepository
import com.edrl.stickerbridge.core.pack.PackSeries
import com.edrl.stickerbridge.core.pack.PackService
import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.core.pack.StickerPackPublisher
import com.edrl.stickerbridge.core.pack.pack
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SaveStickersUseCaseTest {
    private val repository = FakePackRepository()
    private var nextId = 0
    private val packs = PackService(repository, { EncodedFile("tray.png", 1_000) }, { "id${nextId++}" })
    private val publisher = FakePublisher()

    private fun ref(name: String) = ImageRef.Remote("https://p16.tiktokcdn.com/$name")

    /** Converts by name: "anim…" → animated, "drop…" → static with animation dropped, "fail…" → error. */
    private val converter =
        ImageConverter { ref ->
            val name = (ref as ImageRef.Remote).url.substringAfterLast('/')
            when {
                name.startsWith("fail") -> ConversionResult.Failed(ConversionError.DownloadFailed)
                name.startsWith("anim") ->
                    ConversionResult.Converted(
                        ConvertedSticker(EncodedFile("$name.webp", 200_000), PackSeries.ANIMATED, false),
                    )
                name.startsWith("drop") ->
                    ConversionResult.Converted(
                        ConvertedSticker(EncodedFile("$name.webp", 50_000), PackSeries.STATIC, true),
                    )
                else ->
                    ConversionResult.Converted(
                        ConvertedSticker(EncodedFile("$name.webp", 50_000), PackSeries.STATIC, false),
                    )
            }
        }

    private val useCase = SaveStickersUseCase(converter, packs, publisher)

    @Test
    fun `saves what converts, counts what fails and reports per pack`() =
        runTest {
            val result = useCase.save(listOf(ref("anim1"), ref("fail1"), ref("anim2")))

            assertEquals(mapOf("TikTok animados 1" to 2), result.saved)
            assertEquals(1, result.failed)
            assertEquals(0, result.withoutAnimation)
            assertEquals(1, result.actions.size)
        }

    @Test
    fun `a pack with fewer than three stickers waits`() =
        runTest {
            val result = useCase.save(listOf(ref("a"), ref("b")))

            val action = assertIs<PackAction.Waiting>(result.actions.single())
            assertEquals(1, action.missing)
        }

    @Test
    fun `a pack with three stickers opens WhatsApp`() =
        runTest {
            val result = useCase.save(listOf(ref("a"), ref("b"), ref("c")))

            assertEquals("tiktok_static_1", assertIs<PackAction.OpenWhatsApp>(result.actions.single()).pack.identifier)
        }

    @Test
    fun `stickers that lost their animation are counted and go to the static series`() =
        runTest {
            val result = useCase.save(listOf(ref("drop1"), ref("anim1")))

            assertEquals(1, result.withoutAnimation)
            assertEquals(mapOf("TikTok estáticos 1" to 1, "TikTok animados 1" to 1), result.saved)
            assertEquals(2, result.actions.size)
        }

    @Test
    fun `progress is reported after each image`() =
        runTest {
            val progress = mutableListOf<Pair<Int, Int>>()

            useCase.save(listOf(ref("a"), ref("fail"), ref("b"))) { done, total -> progress += done to total }

            assertEquals(listOf(1 to 3, 2 to 3, 3 to 3), progress)
        }

    @Test
    fun `nothing saved means no actions`() =
        runTest {
            val result = useCase.save(listOf(ref("fail1"), ref("fail2")))

            assertEquals(2, result.failed)
            assertTrue(result.actions.isEmpty())
        }

    @Test
    fun `reports whether WhatsApp is installed`() =
        runTest {
            publisher.installed = false

            assertEquals(false, useCase.save(listOf(ref("a"))).whatsAppInstalled)
        }

    @Test
    fun `the pack helper builds valid three sticker packs`() {
        assertEquals(3, pack(stickers = 3).stickers.size)
    }

    private class FakePublisher : StickerPackPublisher {
        var installed = true

        override fun isWhatsAppInstalled() = installed

        override suspend fun isAdded(identifier: String) = false

        override fun notifyChanged(pack: StickerPack) = Unit
    }
}
