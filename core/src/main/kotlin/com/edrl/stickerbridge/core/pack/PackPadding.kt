package com.edrl.stickerbridge.core.pack

/** A sticker as WhatsApp sees it: [fileName] is the name WhatsApp asks for, [sticker] the stored one. */
data class OfferedSticker(
    val fileName: String,
    val sticker: Sticker,
)

/**
 * WhatsApp refuses packs with fewer than 3 stickers. A smaller pack is offered with copies of its
 * first sticker under other names, and WhatsApp shows identical stickers as one (FR5.12). The
 * copies exist only in what is offered; nothing extra is stored.
 */
object PackPadding {
    private const val COPY_PREFIX = "copy"

    fun offered(pack: StickerPack): List<OfferedSticker> {
        val real = pack.stickers.map { OfferedSticker(it.fileName, it) }
        val first = pack.stickers.firstOrNull() ?: return real
        val copies =
            (1..StickerLimits.MIN_STICKERS - real.size).map {
                OfferedSticker(
                    "$COPY_PREFIX${it}_${first.fileName}",
                    first,
                )
            }
        return real + copies
    }
}
