package com.edrl.stickerbridge.core.pack

/** Where a pack stands with respect to WhatsApp (BR-18). It is derived, never stored. */
sealed interface PackStatus {
    data class NeedsMore(
        val missing: Int,
    ) : PackStatus

    data object ReadyToAdd : PackStatus

    data object Added : PackStatus

    companion object {
        fun of(
            pack: StickerPack,
            addedToWhatsApp: Boolean,
        ): PackStatus =
            when {
                pack.stickers.size < StickerLimits.MIN_STICKERS ->
                    NeedsMore(StickerLimits.MIN_STICKERS - pack.stickers.size)
                addedToWhatsApp -> Added
                else -> ReadyToAdd
            }
    }
}
