package com.edrl.stickerbridge.core.pack

/** Where a pack stands with respect to WhatsApp (BR-18). It is derived, never stored. */
sealed interface PackStatus {
    data object ReadyToAdd : PackStatus

    data object Added : PackStatus

    companion object {
        fun of(
            pack: StickerPack,
            addedToWhatsApp: Boolean,
        ): PackStatus = if (addedToWhatsApp) Added else ReadyToAdd
    }
}
