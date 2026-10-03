package com.edrl.stickerbridge.core.pack

/** WhatsApp never mixes static and animated stickers in one pack, so packs come in two series. */
enum class PackSeries { STATIC, ANIMATED }

data class Sticker(
    val id: String,
    val fileName: String,
    val emoji: String,
    val sizeBytes: Long,
)

/**
 * A sticker pack stored on the phone and offered to WhatsApp.
 *
 * [imageDataVersion] tells WhatsApp the content changed; it only ever increases (BR-17).
 */
data class StickerPack(
    val identifier: String,
    val name: String,
    val publisher: String,
    val series: PackSeries,
    val number: Int,
    val imageDataVersion: Int,
    val stickers: List<Sticker>,
) {
    companion object {
        const val TRAY_ICON_FILE = "tray.png"
    }
}

/** The limits WhatsApp enforces on stickers and packs. */
object StickerLimits {
    const val CANVAS_SIZE = 512
    const val STATIC_MAX_BYTES = 100_000L
    const val ANIMATED_MAX_BYTES = 500_000L
    const val ANIMATED_MAX_DURATION_MS = 10_000L
    const val ANIMATED_MIN_FRAME_MS = 8L
    const val TRAY_ICON_SIZE = 96
    const val TRAY_ICON_MAX_BYTES = 50_000L
    const val MIN_STICKERS = 3
    const val MAX_STICKERS = 30
    const val MAX_TEXT_LENGTH = 128
    const val MAX_EMOJIS = 3

    fun maxBytes(series: PackSeries): Long =
        when (series) {
            PackSeries.STATIC -> STATIC_MAX_BYTES
            PackSeries.ANIMATED -> ANIMATED_MAX_BYTES
        }
}

/** Names, identifiers and defaults the app assigns without asking the user (BR-15, BR-16). */
object PackNaming {
    const val PUBLISHER = "TikTok Stickers"
    const val DEFAULT_EMOJI = "😀"

    fun name(
        series: PackSeries,
        number: Int,
    ): String =
        when (series) {
            PackSeries.STATIC -> "TikTok estáticos $number"
            PackSeries.ANIMATED -> "TikTok animados $number"
        }

    fun identifier(
        series: PackSeries,
        number: Int,
    ): String =
        when (series) {
            PackSeries.STATIC -> "tiktok_static_$number"
            PackSeries.ANIMATED -> "tiktok_animated_$number"
        }
}
