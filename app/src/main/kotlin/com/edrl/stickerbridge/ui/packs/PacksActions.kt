package com.edrl.stickerbridge.ui.packs

import com.edrl.stickerbridge.core.pack.Sticker
import com.edrl.stickerbridge.core.pack.StickerPack
import java.io.File

/** Callbacks and file access of the packs screen. */
data class PacksActions(
    val stickerFile: (StickerPack, Sticker) -> File?,
    val onOpenInWhatsApp: (StickerPack) -> Unit,
    val onRemove: (StickerPack, Sticker) -> Unit,
    val onBack: () -> Unit,
)
