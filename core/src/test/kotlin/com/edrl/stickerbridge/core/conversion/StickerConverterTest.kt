package com.edrl.stickerbridge.core.conversion

import com.edrl.stickerbridge.core.pack.PackSeries
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class StickerConverterTest {
    private val ref = ImageRef.Remote("https://p16-sign.tiktokcdn.com/a.webp")

    /** About 30 fps for one second: every cadence limit of the ladder applies. */
    private val shortAnimation = List(30) { 33L }

    @Test
    fun `stops at the first quality that fits the static limit`() =
        runTest {
            val encoder = FakeEncoder(staticSize = { q -> if (q >= 80) 150_000 else 90_000 })
            val converter = StickerConverter(FakeSource(), encoder)

            val result = assertIs<ConversionResult.Converted>(converter.convert(ref))

            assertEquals(listOf(90, 80, 70), encoder.staticQualities)
            assertEquals(90_000L, result.sticker.file.sizeBytes)
            assertEquals(PackSeries.STATIC, result.sticker.series)
            assertFalse(result.sticker.animationDropped)
        }

    @Test
    fun `discards every oversized attempt`() =
        runTest {
            val encoder = FakeEncoder(staticSize = { q -> if (q >= 80) 150_000 else 90_000 })

            StickerConverter(FakeSource(), encoder).convert(ref)

            assertEquals(listOf("static-q90", "static-q80"), encoder.discarded)
        }

    @Test
    fun `fails as too large when no quality fits`() =
        runTest {
            val encoder = FakeEncoder(staticSize = { 200_000 })

            val result = StickerConverter(FakeSource(), encoder).convert(ref)

            assertEquals(ConversionResult.Failed(ConversionError.TooLarge), result)
            assertEquals(StaticQualityLadder.QUALITIES.size, encoder.discarded.size)
        }

    @Test
    fun `uses the fitted placement for the source size`() =
        runTest {
            val encoder = FakeEncoder(staticSize = { 10_000 })

            StickerConverter(FakeSource(width = 300, height = 600), encoder).convert(ref)

            assertEquals(Placement(256, 512, 128, 0), encoder.placements.single())
        }

    @Test
    fun `propagates source failures`() =
        runTest {
            val source = FakeSource(failure = ConversionError.DownloadFailed)

            val result = StickerConverter(source, FakeEncoder(staticSize = { 10_000 })).convert(ref)

            assertEquals(ConversionResult.Failed(ConversionError.DownloadFailed), result)
        }

    @Test
    fun `closes the decoded image`() =
        runTest {
            val source = FakeSource()

            StickerConverter(source, FakeEncoder(staticSize = { 10_000 })).convert(ref)

            assertTrue(source.lastImage!!.closed)
        }

    @Test
    fun `a short animation that fits at once stays animated at its original cadence`() =
        runTest {
            val encoder = FakeEncoder(staticSize = { 10_000 }, animatedSizes = listOf(300_000))

            val result = StickerConverter(FakeSource(durations = shortAnimation), encoder).convert(ref)

            val sticker = assertIs<ConversionResult.Converted>(result).sticker
            assertEquals(PackSeries.ANIMATED, sticker.series)
            assertFalse(sticker.animationDropped)
            assertEquals(listOf(75 to 30), encoder.animatedAttempts)
            assertTrue(encoder.staticQualities.isEmpty())
        }

    @Test
    fun `tries the ladder in order and discards the rejected attempts`() =
        runTest {
            val encoder =
                FakeEncoder(staticSize = { 10_000 }, animatedSizes = List(5) { 600_000L } + 400_000L)

            val result = StickerConverter(FakeSource(durations = shortAnimation), encoder).convert(ref)

            assertEquals(PackSeries.ANIMATED, assertIs<ConversionResult.Converted>(result).sticker.series)
            assertEquals(listOf(75 to 30, 50 to 30, 30 to 30, 50 to 15, 30 to 15, 40 to 10), encoder.animatedAttempts)
            assertEquals(5, encoder.discarded.size)
        }

    @Test
    fun `an animation that never fits falls back to a static sticker with a warning`() =
        runTest {
            val encoder = FakeEncoder(staticSize = { 10_000 }, animatedSizes = List(7) { 600_000L })

            val result = StickerConverter(FakeSource(durations = shortAnimation), encoder).convert(ref)

            val sticker = assertIs<ConversionResult.Converted>(result).sticker
            assertEquals(PackSeries.STATIC, sticker.series)
            assertTrue(sticker.animationDropped)
            assertEquals(7, encoder.discarded.size)
            assertEquals(listOf(90), encoder.staticQualities)
        }

    @Test
    fun `an animation longer than ten seconds is converted as static without trying`() =
        runTest {
            val encoder = FakeEncoder(staticSize = { 10_000 })

            val result = StickerConverter(FakeSource(durations = List(120) { 100L }), encoder).convert(ref)

            val sticker = assertIs<ConversionResult.Converted>(result).sticker
            assertEquals(PackSeries.STATIC, sticker.series)
            assertTrue(sticker.animationDropped)
            assertTrue(encoder.animatedAttempts.isEmpty())
        }

    @Test
    fun `a slow animation skips cadence limits that would change nothing`() =
        runTest {
            val encoder = FakeEncoder(staticSize = { 10_000 }, animatedSizes = List(7) { 600_000L })

            StickerConverter(FakeSource(durations = List(10) { 125L }), encoder).convert(ref)

            assertEquals(listOf(75 to 10, 50 to 10, 30 to 10), encoder.animatedAttempts)
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
        private val durations: List<Long> = listOf(0),
        private val failure: ConversionError? = null,
    ) : ImageSource {
        var lastImage: FakeImage? = null

        override suspend fun open(ref: ImageRef): ImageOutcome {
            failure?.let { return ImageOutcome.Failed(it) }
            val image = FakeImage(SourceImageInfo(width, height, durations))
            lastImage = image
            return ImageOutcome.Opened(image)
        }
    }

    private class FakeEncoder(
        private val staticSize: (Int) -> Long,
        animatedSizes: List<Long> = emptyList(),
    ) : StickerEncoder {
        private val pendingAnimatedSizes = ArrayDeque(animatedSizes)
        val staticQualities = mutableListOf<Int>()

        /** Quality and number of output frames of each animated attempt. */
        val animatedAttempts = mutableListOf<Pair<Int, Int>>()
        val placements = mutableListOf<Placement>()
        val discarded = mutableListOf<String>()

        override suspend fun encodeStatic(
            image: DecodedImage,
            placement: Placement,
            quality: Int,
        ): EncodedFile {
            staticQualities += quality
            if (placements.lastOrNull() != placement) placements += placement
            return EncodedFile(path = "static-q$quality", sizeBytes = staticSize(quality))
        }

        override suspend fun encodeAnimated(
            image: DecodedImage,
            placement: Placement,
            frames: List<SampledFrame>,
            quality: Int,
        ): EncodedFile {
            animatedAttempts += quality to frames.size
            return EncodedFile(
                path = "animated-${animatedAttempts.size}",
                sizeBytes = pendingAnimatedSizes.removeFirst(),
            )
        }

        override fun discard(file: EncodedFile) {
            discarded += file.path
        }
    }
}
