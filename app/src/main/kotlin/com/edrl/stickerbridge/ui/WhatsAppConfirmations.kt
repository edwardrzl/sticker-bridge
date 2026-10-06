package com.edrl.stickerbridge.ui

import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.core.pack.StickerPackPublisher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Packs waiting for WhatsApp's own add or update screen, shown one after another. WhatsApp only
 * reloads an added pack from that screen (FR5.6), so every changed pack goes through it.
 */
class WhatsAppConfirmations(
    private val publisher: StickerPackPublisher,
) {
    private val queue = ArrayDeque<StickerPack>()
    private val mutableCurrent = MutableStateFlow<StickerPack?>(null)

    /** The pack whose WhatsApp screen the UI should open now. */
    val current: StateFlow<StickerPack?> = mutableCurrent.asStateFlow()

    fun enqueue(packs: List<StickerPack>) {
        packs.filterNot { pack -> queue.any { it.identifier == pack.identifier } }.forEach(queue::addLast)
        if (mutableCurrent.value == null) showNext()
    }

    /** The UI opened WhatsApp's screen for [current]. */
    fun onLaunched() {
        mutableCurrent.value = null
    }

    /** WhatsApp's screen was closed; the next pack, if any, becomes [current]. */
    fun onFinished() = showNext()

    private fun showNext() {
        val next = queue.removeFirstOrNull() ?: return
        publisher.notifyChanged(next)
        mutableCurrent.value = next
    }
}
