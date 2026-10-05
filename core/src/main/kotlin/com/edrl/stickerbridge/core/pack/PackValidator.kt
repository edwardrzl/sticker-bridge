package com.edrl.stickerbridge.core.pack

sealed interface PackViolation {
    data class TooFewStickers(
        val count: Int,
    ) : PackViolation

    data class TooManyStickers(
        val count: Int,
    ) : PackViolation

    data class InvalidIdentifier(
        val identifier: String,
    ) : PackViolation

    data class InvalidText(
        val field: String,
    ) : PackViolation

    data class StickerTooLarge(
        val fileName: String,
        val sizeBytes: Long,
    ) : PackViolation

    data class MissingEmoji(
        val fileName: String,
    ) : PackViolation
}

/** Checks a pack against WhatsApp's limits before it is offered to WhatsApp (BR-19). */
object PackValidator {
    private val IDENTIFIER = Regex("^[A-Za-z0-9_.-]{1,${StickerLimits.MAX_TEXT_LENGTH}}$")

    fun validate(pack: StickerPack): List<PackViolation> =
        buildList {
            val count = pack.stickers.size
            // One sticker is enough: a small pack is padded when offered to WhatsApp (PackPadding).
            if (count == 0) add(PackViolation.TooFewStickers(count))
            if (count > StickerLimits.MAX_STICKERS) add(PackViolation.TooManyStickers(count))
            if (!IDENTIFIER.matches(pack.identifier)) add(PackViolation.InvalidIdentifier(pack.identifier))
            if (!isValidText(pack.name)) add(PackViolation.InvalidText("name"))
            if (!isValidText(pack.publisher)) add(PackViolation.InvalidText("publisher"))

            val maxBytes = StickerLimits.maxBytes(pack.series)
            pack.stickers.forEach { sticker ->
                if (sticker.sizeBytes > maxBytes) {
                    add(PackViolation.StickerTooLarge(sticker.fileName, sticker.sizeBytes))
                }
                if (sticker.emoji.isBlank()) add(PackViolation.MissingEmoji(sticker.fileName))
            }
        }

    private fun isValidText(text: String): Boolean = text.isNotBlank() && text.length <= StickerLimits.MAX_TEXT_LENGTH
}
