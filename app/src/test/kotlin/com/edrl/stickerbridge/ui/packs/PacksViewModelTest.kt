package com.edrl.stickerbridge.ui.packs

import com.edrl.stickerbridge.core.conversion.ConvertedSticker
import com.edrl.stickerbridge.core.conversion.EncodedFile
import com.edrl.stickerbridge.core.pack.PackSeries
import com.edrl.stickerbridge.core.pack.PackStatus
import com.edrl.stickerbridge.ui.FakePublisher
import com.edrl.stickerbridge.ui.InMemoryPackRepository
import com.edrl.stickerbridge.ui.WhatsAppConfirmations
import com.edrl.stickerbridge.ui.packService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class PacksViewModelTest {
    private val repository = InMemoryPackRepository()
    private val packs = packService(repository)
    private val publisher = FakePublisher()
    private val vm = PacksViewModel(packs, publisher, WhatsAppConfirmations(publisher))

    private suspend fun addStatic(count: Int) =
        repeat(count) { packs.addSticker(ConvertedSticker(EncodedFile("$it.webp", 10_000), PackSeries.STATIC, false)) }

    @BeforeEach
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `status combines sticker count and what WhatsApp says`() =
        runTest {
            addStatic(3)
            publisher.added += "tiktok_static_1"

            vm.refresh()

            assertEquals(
                PackStatus.Added,
                vm.state.value.packs
                    .single()
                    .status,
            )
        }

    @Test
    fun `a pack with a single sticker is ready to add`() =
        runTest {
            addStatic(1)

            vm.refresh()

            assertEquals(
                PackStatus.ReadyToAdd,
                vm.state.value.packs
                    .single()
                    .status,
            )
        }

    @Test
    fun `removing from an added pack asks WhatsApp to update it`() =
        runTest {
            addStatic(4)
            publisher.added += "tiktok_static_1"

            vm.remove("tiktok_static_1", "id1")

            assertEquals(
                3,
                vm.state.value.packs
                    .single()
                    .pack.stickers.size,
            )
            assertEquals(
                "tiktok_static_1",
                vm.confirmations.current.value
                    ?.identifier,
            )
        }

    @Test
    fun `removing from a pack not in WhatsApp asks nothing`() =
        runTest {
            addStatic(4)

            vm.remove("tiktok_static_1", "id1")

            assertNull(vm.confirmations.current.value)
        }

    @Test
    fun `a pack can be opened in WhatsApp once it has three stickers`() =
        runTest {
            addStatic(3)
            vm.refresh()

            vm.openInWhatsApp(
                vm.state.value.packs
                    .single()
                    .pack,
            )

            assertEquals(
                "tiktok_static_1",
                vm.confirmations.current.value
                    ?.identifier,
            )
        }
}
