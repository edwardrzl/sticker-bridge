package com.edrl.stickerbridge.ui.search

import com.edrl.stickerbridge.core.conversion.ImageRef
import com.edrl.stickerbridge.core.extraction.ExtractionError
import com.edrl.stickerbridge.core.extraction.ExtractionOutcome
import com.edrl.stickerbridge.core.save.SaveStickersUseCase
import com.edrl.stickerbridge.ui.FakeExtractor
import com.edrl.stickerbridge.ui.FakePublisher
import com.edrl.stickerbridge.ui.InMemoryPackRepository
import com.edrl.stickerbridge.ui.RecordingConverter
import com.edrl.stickerbridge.ui.WhatsAppConfirmations
import com.edrl.stickerbridge.ui.image
import com.edrl.stickerbridge.ui.loaded
import com.edrl.stickerbridge.ui.packService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val link = "https://vt.tiktok.com/ZSb9e5Ndq/"
    private val converter = RecordingConverter()
    private val publisher = FakePublisher()
    private val saveStickers = SaveStickersUseCase(converter, packService(InMemoryPackRepository()), publisher)

    private fun viewModel(extractor: FakeExtractor) =
        SearchViewModel(extractor, saveStickers, WhatsAppConfirmations(publisher))

    @BeforeEach
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `an invalid link opens no session and says so`() {
        val extractor = FakeExtractor()
        val vm = viewModel(extractor)

        vm.search("https://www.tiktok.com/@someone")

        assertEquals(SearchPhase.InvalidLink, vm.state.value.phase)
        assertTrue(extractor.opened.isEmpty())
    }

    @Test
    fun `images arrive ordered and nothing is chosen yet`() {
        val vm = viewModel(FakeExtractor(loaded(image("a", 53), image("b", 19))))

        vm.search(link)

        assertEquals(SearchPhase.Loaded, vm.state.value.phase)
        assertEquals(
            listOf(53L, 19L),
            vm.state.value.images
                .map { it.likes },
        )
        assertTrue(
            vm.state.value.selected
                .isEmpty(),
        )
    }

    @Test
    fun `tapping toggles the choice`() {
        val vm = viewModel(FakeExtractor(loaded(image("a", 53), image("b", 19))))
        vm.search(link)
        val (a, b) = vm.state.value.images

        vm.toggle(a.url)
        vm.toggle(b.url)
        vm.toggle(a.url)

        assertEquals(setOf(b.url), vm.state.value.selected)
    }

    @Test
    fun `saving without a choice does nothing`() {
        val vm = viewModel(FakeExtractor(loaded(image("a", 53))))
        vm.search(link)

        vm.saveSelected()

        assertEquals(SearchPhase.Loaded, vm.state.value.phase)
        assertTrue(converter.converted.isEmpty())
    }

    @Test
    fun `only the chosen images are saved, most liked first`() {
        val vm = viewModel(FakeExtractor(loaded(image("a", 53), image("b", 19), image("c", 3))))
        vm.search(link)
        val (a, _, c) = vm.state.value.images
        vm.toggle(c.url)
        vm.toggle(a.url)

        vm.saveSelected()

        assertEquals(listOf<ImageRef>(ImageRef.Remote(a.url), ImageRef.Remote(c.url)), converter.converted)
        val saved = assertIs<SearchPhase.Saved>(vm.state.value.phase)
        assertEquals(mapOf("TikTok estáticos 1" to 2), saved.result.saved)
        assertTrue(
            vm.state.value.selected
                .isEmpty(),
        )
    }

    @Test
    fun `loading more keeps the choice`() {
        val vm = viewModel(FakeExtractor(loaded(image("a", 5), hasMore = true), loaded(image("z", 90), image("a", 5))))
        vm.search(link)
        val a =
            vm.state.value.images
                .single()
        vm.toggle(a.url)

        vm.loadMore()

        assertEquals(
            listOf(90L, 5L),
            vm.state.value.images
                .map { it.likes },
        )
        assertEquals(setOf(a.url), vm.state.value.selected)
        assertEquals(false, vm.state.value.hasMore)
    }

    @Test
    fun `a failed search shows its error and can be retried`() {
        val extractor = FakeExtractor(ExtractionOutcome.Failed(ExtractionError.NoConnection), loaded(image("a", 1)))
        val vm = viewModel(extractor)

        vm.search(link)
        assertEquals(SearchPhase.Failed(ExtractionError.NoConnection), vm.state.value.phase)

        vm.retry()
        assertEquals(SearchPhase.Loaded, vm.state.value.phase)
        assertEquals(2, extractor.opened.size)
    }

    @Test
    fun `a pack ready for WhatsApp is offered once after saving`() {
        val vm = viewModel(FakeExtractor(loaded(image("a", 3), image("b", 2), image("c", 1))))
        vm.search(link)
        vm.state.value.images
            .forEach { vm.toggle(it.url) }

        vm.saveSelected()

        assertEquals(
            "tiktok_static_1",
            vm.confirmations.current.value
                ?.identifier,
        )
        vm.confirmations.onLaunched()
        vm.confirmations.onFinished()
        assertEquals(null, vm.confirmations.current.value)
    }

    @Test
    fun `imported gallery images go through the same save`() {
        val vm = viewModel(FakeExtractor())

        vm.saveImported(listOf("content://media/picker/1"))

        assertEquals(listOf<ImageRef>(ImageRef.Local("content://media/picker/1")), converter.converted)
        assertIs<SearchPhase.Saved>(vm.state.value.phase)
    }

    @Test
    fun `cancelling closes the session and returns to idle`() {
        val extractor = FakeExtractor(loaded(image("a", 1)))
        val vm = viewModel(extractor)
        vm.search(link)

        vm.cancel()

        assertEquals(SearchPhase.Idle, vm.state.value.phase)
        assertEquals(1, extractor.closed)
    }
}
