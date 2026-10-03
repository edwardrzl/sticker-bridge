package com.edrl.stickerbridge.core.conversion

import com.edrl.stickerbridge.core.pack.PackSeries
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class StickerConverterTest {
    private val ref = ImageRef.Remote("https://p16-sign.tiktokcdn.com/a.webp")

    @Test
    fun `stops at the first quality that fits the static limit`() =
        runTest {
            val encoder = FakeEncoder(sizeForQuality = { q -> if (q >= 80) 150_000 else 90_000 })
            val converter = StickerConverter(FakeSource(), encoder)

            val result = assertIs<ConversionResult.Converted>(converter.convert(ref))

            assertEquals(listOf(90, 80, 70), encoder.qualitiesTried)
            assertEquals(90_000L, result.sticker.file.sizeBytes)
            assertEquals(PackSeries.STATIC, result.sticker.series)
        }

    @Test
    fun `discards every oversized attempt`() =
        runTest {
            val encoder = FakeEncoder(sizeForQuality = { q -> if (q >= 80) 150_000 else 90_000 })

            StickerConverter(FakeSource(), encoder).convert(ref)

            assertEquals(listOf("q90", "q80"), encoder.discarded)
        }

    @Test
    fun `fails as too large when no quality fits`() =
        runTest {
            val encoder = FakeEncoder(sizeForQuality = { 200_000 })

            val result = StickerConverter(FakeSource(), encoder).convert(ref)

            assertEquals(ConversionResult.Failed(ConversionError.TooLarge), result)
            assertEquals(StaticQualityLadder.QUALITIES.size, encoder.discarded.size)
        }

    @Test
    fun `uses the fitted placement for the source size`() =
        runTest {
            val encoder = FakeEncoder(sizeForQuality = { 10_000 })

            StickerConverter(FakeSource(width = 300, height = 600), encoder).convert(ref)

            assertEquals(Placement(256, 512, 128, 0), encoder.placements.single())
        }

    @Test
    fun `propagates source failures`() =
        runTest {
            val source = FakeSource(failure = ConversionError.DownloadFailed)

            val result = StickerConverter(source, FakeEncoder { 10_000 }).convert(ref)

            assertEquals(ConversionResult.Failed(ConversionError.DownloadFailed), result)
        }

    @Test
    fun `closes the decoded image`() =
        runTest {
            val source = FakeSource()

            StickerConverter(source, FakeEncoder { 10_000 }).convert(ref)

            assertTrue(source.lastImage!!.closed)
        }

    private class FakeImage(
        override val info: SourceImageInfo,
    ) : DecodedImage {
        var closed = false

        override fun close() {
            closed = true
        }
    }

    private class FakeSource(
        private val width: Int = 400,
        private val height: Int = 400,
        private val failure: ConversionError? = null,
    ) : ImageSource {
        var lastImage: FakeImage? = null

        override suspend fun open(ref: ImageRef): ImageOutcome {
            failure?.let { return ImageOutcome.Failed(it) }
            val image = FakeImage(SourceImageInfo(width, height, frameCount = 1, totalDurationMs = 0))
            lastImage = image
            return ImageOutcome.Opened(image)
        }
    }

    private class FakeEncoder(
        private val sizeForQuality: (Int) -> Long,
    ) : StickerEncoder {
        val qualitiesTried = mutableListOf<Int>()
        val placements = mutableListOf<Placement>()
        val discarded = mutableListOf<String>()

        override suspend fun encodeStatic(
            image: DecodedImage,
            placement: Placement,
            quality: Int,
        ): EncodedFile {
            qualitiesTried += quality
            if (placements.lastOrNull() != placement) placements += placement
            return EncodedFile(path = "q$quality", sizeBytes = sizeForQuality(quality))
        }

        override fun discard(file: EncodedFile) {
            discarded += file.path
        }
    }
}
