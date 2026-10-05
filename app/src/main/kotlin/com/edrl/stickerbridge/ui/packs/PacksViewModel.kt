package com.edrl.stickerbridge.ui.packs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edrl.stickerbridge.core.pack.PackService
import com.edrl.stickerbridge.core.pack.PackStatus
import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.core.pack.StickerPackPublisher
import com.edrl.stickerbridge.core.pack.StorageException
import com.edrl.stickerbridge.ui.WhatsAppConfirmations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PackItem(
    val pack: StickerPack,
    val status: PackStatus,
)

data class PacksState(
    val packs: List<PackItem> = emptyList(),
    val loading: Boolean = true,
    val storageFailed: Boolean = false,
)

/** "My packs": every pack with its WhatsApp status (FR5.8), and removing stickers (FR5.11). */
class PacksViewModel(
    private val packService: PackService,
    private val publisher: StickerPackPublisher,
    val confirmations: WhatsAppConfirmations,
) : ViewModel() {
    private val mutableState = MutableStateFlow(PacksState())
    val state: StateFlow<PacksState> = mutableState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            mutableState.value =
                try {
                    PacksState(packs = packService.listPacks().map { item(it) }, loading = false)
                } catch (_: StorageException) {
                    PacksState(loading = false, storageFailed = true)
                }
        }
    }

    /** Opens WhatsApp's add or update screen for a pack. */
    fun openInWhatsApp(pack: StickerPack) {
        if (pack.stickers.isNotEmpty()) confirmations.enqueue(listOf(pack))
    }

    /** Removes a sticker; an added pack then goes through WhatsApp's update screen (FR5.6, FR5.11). */
    fun remove(
        packIdentifier: String,
        stickerId: String,
    ) {
        viewModelScope.launch {
            try {
                val wasAdded = publisher.isAdded(packIdentifier)
                val updated = packService.removeSticker(packIdentifier, stickerId)
                if (wasAdded && updated != null && updated.stickers.isNotEmpty()) {
                    confirmations.enqueue(listOf(updated))
                }
            } catch (_: StorageException) {
                mutableState.update { it.copy(storageFailed = true) }
            }
            refresh()
        }
    }

    private suspend fun item(pack: StickerPack) =
        PackItem(pack, PackStatus.of(pack, publisher.isAdded(pack.identifier)))
}
